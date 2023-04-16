import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.failureConditions.BuildFailureOnText
import jetbrains.buildServer.configs.kotlin.v2018_2.failureConditions.failOnText
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.1"

project {

    buildType(id1BuildPackage)
    buildType(id2PublishPackage)

    template(DockerApiAutomationTests)
    template(DockerBuildAndPackage)
    template(DockerPublishPackageLinux)

    params {
        param("env.ASSETESSENTIALS_ADMIN_USERNAME", "admin")
        param("env.ASSETESSENTIALS_REDIRECT_URI", "https://assetessentials-%EnvironmentUrl%.dudesoln.com/IdentityAutomation")
        param("ProjectName", "identity")
        param("env.ASSETESSENTIALS_ADMIN_PASSWORD", "credentialsJSON:05611192-09b1-4b88-b009-4721481ab66f")
        param("FetchURL", "git@bitbucket.org:DudeSolutions/ae-citizenportalweb.git")
        param("MinorVersion", "")
        param("env.ASSETESSENTIALS_ACCOUNTID", "DDDF48B4-7363-4E94-8DD8-08D588424EED")
        param("BuildNumberFormat", "%MajorVersion%.%MinorVersion%.%autoinc.AssetEssentials_CitizenPortal%%VersionSuffix%")
    }
    buildTypesOrder = arrayListOf(id1BuildPackage, id2PublishPackage)
}

object id1BuildPackage : BuildType({
    templates(DockerBuildAndPackage)
    id("1BuildPackage")
    name = "1. Build & Package"

    buildNumberPattern = "%BuildNumberFormat%"

    params {
        param("MajorVersion", "1")
        param("SolutionFile", "")
        param("env.AWS_SECRET_ACCESS_KEY", "%AWS_704173473619_ECR_SECRET_ACCESS_KEY%")
        param("ProjectName", "citizenportalweb")
        param("env.AWS_ACCESS_KEY_ID", "%AWS_704173473619_ECR_ACCESS_KEY_ID%")
        param("EcrRepositoryUrl", "704173473619.dkr.ecr.us-east-1.amazonaws.com/ae-citizenportalweb")
        param("MinorVersion", "2")
        param("BranchSpecification", """
            +:refs/heads/feature/*
            +:refs/heads/bugfix/*
            +:refs/heads/DECO/*
        """.trimIndent())
        param("env.AWS_DEFAULT_REGION", "us-east-1")
        param("Market", "AE")
        param("env.MyGetNpmPreAuthUrl", "%MyGetNpmPreAuthUrl%")
    }

    steps {
        script {
            name = "Docker Tag"
            id = "RUNNER_170"
            enabled = false
            scriptContent = """
                echo %ProjectName% 
                echo %DockerTag%
                echo %DockerLatestEnvironmentTag%
                
                if(%teamcity.build.branch.is_default%)
                then
                   docker tag %ProjectName% %DockerTag%
                   docker tag %ProjectName% %DockerLatestEnvironmentTag%
                else
                echo %teamcity.build.branch% is not the default branch, skipping tagging step.
                fi
            """.trimIndent()
        }
        script {
            name = "Docker Push Versioned ChrisTest"
            id = "RUNNER_668"
            enabled = false
            scriptContent = """
                echo %DockerTag%
                echo %DockerLatestEnvironmentTag%
                
                if(%teamcity.build.branch.is_default%)
                then
                
                   docker tag %ProjectName% dtr.dudesoln.com/dp/identityapi:orange
                   docker push dtr.dudesoln.com/dp/identityapi:orange
                
                   docker tag %ProjectName% %DockerTag%
                   docker push %DockerTag% 
                
                else
                echo %teamcity.build.branch% is not the default branch, skipping push.
                fi
            """.trimIndent()
        }
        script {
            name = "Docker Push Versioned to ECR"
            id = "RUNNER_829"
            scriptContent = """
                if(%teamcity.build.branch.is_default%)
                then
                	echo "AWS Identity: ${'$'}(aws sts get-caller-identity)"
                	aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin %EcrRepositoryUrl%
                	Tag=${'$'}(echo "%EcrRepositoryUrl%:%build.number%" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')
                	echo ${'$'}Tag
                	docker tag %ProjectName% ${'$'}Tag
                	docker push ${'$'}Tag
                else
                echo %teamcity.build.branch% is not the default branch, skipping push.
                fi
            """.trimIndent()
        }
        stepsOrder = arrayListOf("RUNNER_44", "RUNNER_122", "RUNNER_65", "RUNNER_319", "RUNNER_69", "RUNNER_329", "RUNNER_71", "RUNNER_43", "RUNNER_170", "RUNNER_165", "RUNNER_668", "RUNNER_829")
    }
    
    disableSettings("RUNNER_43")
})

object id2PublishPackage : BuildType({
    templates(DockerPublishPackageLinux)
    id("2PublishPackage")
    name = "2. Publish Package"

    params {
        param("FetchURL", "https://code.dudesoln.com/scm/ae/citizen.git")
        param("DependencyBuildNumber", "${id1BuildPackage.depParamRefs.buildNumber}")
        param("ProjectName", "citizenportalweb")
        param("OctopusProjectName", "citizenportalweb")
    }

    steps {
        powerShell {
            name = "Deploy Jenkins"
            id = "RUNNER_841"
            scriptMode = script {
                content = """
                    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 
                    # Get user crumb    
                    ${'$'}JENKINS_URL='jenkins-lzdev.dudesoln.com/'
                    ${'$'}UserName="%jenkins_user%"
                    echo ${'$'}UserName
                    ${'$'}JenkinsAPIToken="%jenkins_token%"
                    ${'$'}jenkins_project="ae-citizenportalweb"
                    ${'$'}environment="%aws_environment%"
                    ${'$'}tag="%build.number%"
                    ${'$'}headers = ${'$'}null
                    ${'$'}headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
                    ${'$'}headers.Add("Authorization", "Basic " + [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("${'$'}(${'$'}UserName):${'$'}JenkinsAPIToken"))) 
                    ${'$'}crumb_url = "https://${'$'}(${'$'}JENKINS_URL)/crumbIssuer/api/xml"
                    [xml]${'$'}crumbs = Invoke-WebRequest ${'$'}crumb_url -Method GET  -Headers ${'$'}headers
                    
                    # Get Next Jenkins build id
                    ${'$'}buildUrl= "https://" + ${'$'}JENKINS_URL + "job/" + ${'$'}jenkins_project + "/api/json" 
                    ${'$'}nextBuild=(Invoke-WebRequest ${'$'}buildUrl | ConvertFrom-Json).nextBuildNumber
                    
                    # Invoke Jenkins job
                    ${'$'}headers.Add(${'$'}crumbs.defaultCrumbIssuer.crumbRequestField, ${'$'}crumbs.defaultCrumbIssuer.crumb)
                    ${'$'}headers.Add("Accept", "application/xml")
                    ${'$'}url="https://" + ${'$'}JENKINS_URL + "job/" + ${'$'}jenkins_project + "/buildWithParameters?ENVIRONMENT=${'$'}{environment}&IMAGE_TAG=${'$'}{tag}"
                    Invoke-WebRequest ${'$'}url -Method POST -Headers ${'$'}headers -ContentType "application/json"
                    
                    ${'$'}statusUrl= "https://" + ${'$'}JENKINS_URL + "job/" + ${'$'}jenkins_project + "/" + ${'$'}nextBuild + "/api/json" 
                    while(${'$'}true)
                    {
                        Start-Sleep -seconds 10    
                        ${'$'}statReq = (Invoke-WebRequest -Uri ${'$'}statusUrl) 
                        #write-host ${'$'}statReq
                        ${'$'}jsResult = ${'$'}statReq | ConvertFrom-Json
                        
                        write-host "Result: " ${'$'}(if (${'$'}jsResult.result) {${'$'}jsResult.result} else {"running.."})
                        if (${'$'}jsResult.result -eq 'SUCCESS' )
                        {
                            break;
                        }
                        else {
                            if ((${'$'}jsResult.result -eq 'FAILURE') -or (${'$'}jsResult.result -eq 'ABORTED')) {
                                Throw "Jenkins job failed!"
                            }        
                        }
                    }
                """.trimIndent()
            }
        }
    }

    triggers {
        finishBuildTrigger {
            id = "TRIGGER_23"
            buildType = "${id1BuildPackage.id}"
        }
    }

    failureConditions {
        failOnText {
            id = "BUILD_EXT_48"
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "Error"
            reverse = false
            stopBuildOnFailure = true
        }
    }

    dependencies {
        snapshot(id1BuildPackage) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
    
    disableSettings("RUNNER_272")
})

object DockerApiAutomationTests : Template({
    name = "DockerApiAutomationTests"

    artifactRules = """
        AutomationTests/results => results
        Automation/results => results
    """.trimIndent()
    buildNumberPattern = "%DependencyBuildNumber%"

    params {
        param("DefaultBranch", "refs/heads/****")
        param("RakeTags", "tags='~@api_placeholder1,~@api_placeholder2'")
        param("env.JASMINE_TIMEOUT_MS", "15000")
        param("BranchSpecification", " ")
        param("AutomationTestTimeout", "15")
        param("NpmSuite", "all")
        param("WaitForDockerSleep", "60")
        param("RakeDebug", "false")
        param("EnvironmentUrl", "")
        param("TestRole", "api_core_")
        param("DependencyBuildNumber", "")
        param("RakeTaskName", "")
        param("env.LOG_LEVEL", "info")
    }

    vcs {
        root(DslContext.settingsRoot)

        checkoutMode = CheckoutMode.ON_SERVER
        cleanCheckout = true
    }

    steps {
        script {
            name = "NPM Set Cache Location"
            id = "RUNNER_2271"
            scriptContent = """
                export PATH=${'$'}PATH:/root/.nvm/versions/node/v6.9.4/bin
                npm config set cache /TeamCity/BuildAgent/temp/npm-cache --global
            """.trimIndent()
        }
        script {
            name = "Add MyGet Access Token"
            id = "RUNNER_169"
            scriptContent = """sed -i "/_authToken=/ c \/\/dsi.myget.org\/F\/dev\/npm\/:_authToken=%MyGetAccessToken%" ~/.npmrc"""
        }
        script {
            name = "NPM Install"
            id = "RUNNER_2272"
            workingDir = "Automation"
            scriptContent = """
                export PATH=${'$'}PATH:/root/.nvm/versions/node/v6.9.4/bin
                npm install
            """.trimIndent()
        }
        script {
            name = "Kill MyGet Access Token"
            id = "RUNNER_171"
            scriptContent = """sed -i "/_authToken=/ c \/\/dsi.myget.org\/F\/dev\/npm\/:_authToken=" ~/.npmrc"""
        }
        script {
            name = "Wait For Docker"
            id = "RUNNER_88"
            scriptContent = "sleep %WaitForDockerSleep%"
        }
        script {
            name = "Npm Test"
            id = "RUNNER_2273"
            workingDir = "Automation"
            scriptContent = """
                export PATH=${'$'}PATH:/root/.nvm/versions/node/v6.9.4/bin
                npm test -- --suite=%NpmSuite%
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            id = "vcsTrigger"
            triggerRules = """
                -:.
                +:AutomationBroken
            """.trimIndent()
            branchFilter = ""
        }
    }

    failureConditions {
        failOnText {
            id = "BUILD_EXT_2128"
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "(Cucumber::Ambiguous)"
            failureMessage = "Ambiguous step definition found"
            reverse = false
        }
        failOnText {
            id = "BUILD_EXT_2150"
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "rake aborted"
            failureMessage = "Rake aborted"
            reverse = false
        }
        failOnText {
            id = "BUILD_EXT_7"
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "npm ERR!"
            reverse = false
        }
        failOnText {
            id = "BUILD_EXT_21"
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "FullyQualifiedError"
            failureMessage = "FullyQualifiedError"
            reverse = false
        }
        failOnText {
            id = "BUILD_EXT_24"
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "missing argument"
            failureMessage = "missing argument"
            reverse = false
        }
    }

    features {
        feature {
            id = "BUILD_EXT_128"
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "+:**/**/TEST*.xml")
            param("xmlReportParsing.verboseOutput", "true")
        }
    }

    requirements {
        contains("system.agent.name", "DudePlatformLinux", "RQ_5")
    }
})

object DockerBuildAndPackage : Template({
    name = "DockerBuildAndPackage"

    buildNumberPattern = "%BuildNumberFormat%%VersionSuffix%"

    params {
        param("CheckoutRules", ".")
        param("DockerLatestEnvironmentTag", "**Parameter set dynamically***")
        param("DockerTag", "***Parameter set dynamically***")
        param("DockerLatestTag", "***Parameter set dynamically***")
    }

    vcs {
        root(DslContext.settingsRoot)

        checkoutMode = CheckoutMode.ON_SERVER
        cleanCheckout = true
    }

    steps {
        script {
            name = "Set Dynamic Parameters"
            id = "RUNNER_44"
            scriptContent = """
                varProjectName=${'$'}(echo %ProjectName% | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')
                echo "##teamcity[setParameter name='ProjectName' value='${'$'}{varProjectName}']"
                echo %ProjectName%
                
                varTag=${'$'}(echo "%DockerPrivateRegistryHost%/%Market%/${'$'}{varProjectName}:%build.number%" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')
                echo ${'$'}varTag
                echo "##teamcity[setParameter name='DockerTag' value='${'$'}{varTag}']"
                
                varLatestTag=${'$'}(echo "%DockerPrivateRegistryHost%/%Market%/${'$'}{varProjectName}:latest" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')
                echo ${'$'}varLatestTag
                echo "##teamcity[setParameter name='DockerLatestTag' value='${'$'}{varLatestTag}']"
                
                varLatestEnvironmentTag=${'$'}(echo "%DockerPrivateRegistryHost%/%Market%/%ProjectName%:latest-%EnvironmentUrl%" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]')
                echo ${'$'}varLatestEnvironmentTag
                echo "##teamcity[setParameter name='DockerLatestEnvironmentTag' value='${'$'}{varLatestEnvironmentTag}']"
            """.trimIndent()
        }
        script {
            name = "Docker Login"
            id = "RUNNER_122"
            scriptContent = """
                rm -rf ${'$'}HOME/.docker/config.json
                
                docker logout https://%DockerPrivateRegistryHost%
                docker login -u octopuslinux -p %OctopusLinuxPassword% https://%DockerPrivateRegistryHost%
                docker login -u octopuslinux -p %OctopusLinuxPassword% https://%DockerPrivateRegistryHost%
                docker login -u octopuslinux -p %OctopusLinuxPassword% https://%DockerPrivateRegistryHost%
                docker login -u octopuslinux -p %OctopusLinuxPassword% https://%DockerPrivateRegistryHost%
                docker login -u octopuslinux -p %OctopusLinuxPassword% https://%DockerPrivateRegistryHost%
            """.trimIndent()
        }
        script {
            name = "Create Nuget Config File"
            id = "RUNNER_65"
            scriptContent = """echo "<configuration><packageSources><clear/><add key=\"myget.org\" value=\"%MyGetPreAuthUrl%\" protocolVersion=\"3\" /></packageSources></configuration>" > /root/.nuget/NuGet/NuGet.Config"""
        }
        script {
            name = "Create npmrc"
            id = "RUNNER_319"
            scriptContent = """echo "@dude:registry=%MyGetNpmPreAuthUrl%" > .npmrc"""
        }
        script {
            name = "Run Build Script"
            id = "RUNNER_69"
            scriptContent = """
                export PATH=${'$'}PATH:/root/.nvm/versions/node/v6.9.4/bin
                chmod +x *.sh
                ./build.sh
            """.trimIndent()
        }
        script {
            name = "Remove npmrc"
            id = "RUNNER_329"
            scriptContent = "rm -f .npmrc"
        }
        script {
            name = "Delete Nuget Config File"
            id = "RUNNER_71"
            scriptContent = """echo "<configuration><packageSources><add key=\"myget.org\" value=\"\" protocolVersion=\"3\" /></packageSources></configuration>" > /root/.nuget/NuGet/NuGet.Config"""
        }
        script {
            name = "Docker Build"
            id = "RUNNER_43"
            scriptContent = "docker build --tag %DockerTag% ."
        }
        script {
            name = "Add New Repos"
            id = "RUNNER_403"
            enabled = false
            scriptContent = """
                echo "Running test against service, adding DTR repo if service does not exist..."
                
                curl -u octopuslinux:%OctopusLinuxPassword% \
                -X POST \
                https://dtr.dudesoln.com/api/v0/repositories/dp \
                -H 'content-type: application/json' \
                -d '{"enableManifestLists":false,
                     "immutableTags":false,
                     "name":"%ProjectName%",
                     "scanOnPush":false,
                     "shortDescription":"Created automatically with TeamCity",
                     "visibility":"public"
                }'
            """.trimIndent()
        }
        script {
            name = "Docker Tag"
            id = "RUNNER_170"
            enabled = false
            scriptContent = """
                echo %ProjectName% 
                echo %DockerTag%
                echo %DockerLatestEnvironmentTag%
                
                docker tag %ProjectName% %DockerTag%
                docker tag %ProjectName% %DockerLatestEnvironmentTag%
            """.trimIndent()
        }
        script {
            name = "Docker Push Versioned"
            id = "RUNNER_165"
            scriptContent = """
                echo %DockerTag%
                echo %DockerLatestEnvironmentTag%
                
                if(%teamcity.build.branch.is_default%)
                then
                
                   docker tag %ProjectName% %DockerLatestEnvironmentTag%
                   docker push %DockerLatestEnvironmentTag%
                
                   docker tag %ProjectName% %DockerTag%
                   docker push %DockerTag% 
                
                else
                echo %teamcity.build.branch% is not the default branch, skipping push.
                fi
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            id = "vcsTrigger"
            branchFilter = ""
        }
    }

    features {
        feature {
            id = "BUILD_EXT_1453"
            type = "teamcity.stash.status"
            param("stash_host", "%StashUrl%")
            param("stash_username", "%StashUser%")
            param("stash_failCancelledBuilds", "true")
            param("secure:stash_username", "credentialsJSON:e381958d-dbe2-402c-8149-615b8d312b3b")
        }
    }

    requirements {
        contains("system.agent.name", "DudePlatformLinux", "RQ_6")
    }
})

object DockerPublishPackageLinux : Template({
    name = "DockerPublishPackageLinux"

    buildNumberPattern = "%DependencyBuildNumber%"

    params {
        param("OctopusProjectName", "")
    }

    steps {
        step {
            name = "Create and Deploy Release"
            id = "RUNNER_272"
            type = "octopus.create.release"
            param("octopus_additionalcommandlinearguments", "--packageversion %build.number% --deploymenttimeout=00:20:00")
            param("octopus_waitfordeployments", "true")
            param("octopus_version", "3.0+")
            param("octopus_host", "%OctopusUrl%")
            param("octopus_project_name", "%OctopusProjectName%")
            param("octopus_deployto", "%OctopusEnvironmentName%")
            param("secure:octopus_apikey", "credentialsJSON:29f1398f-59e0-4226-956b-e8bf0ee87def")
            param("octopus_releasenumber", "%build.number%")
        }
    }
})
