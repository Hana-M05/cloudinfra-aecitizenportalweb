@Library('shared-lib@rosa_pipeline/CE-811') _

if (env.JENKINS_URL.contains('10.151.34.70')) {
    rosa_pipeline(
        Rosa_Lower_Environments: ["ae-us-gov-dev"],
        Rosa_Upper_Environments: ["ae-us-gov-prod"],
        Repo_name: "ae-citizenportalweb"
    )
} else {
    main_pipeline(
        Upper_Environemnts: ["staging","prod","demo","training", "produk"],
        Lower_Environemnts: ["master","apple","orange","blue","perf","int1","int2","int3","int4","int5","int6","int7","int8"],
        ALB_PREFIX: "cpwebalb",
    )
}
