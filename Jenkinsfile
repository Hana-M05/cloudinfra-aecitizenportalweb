@Library('shared-lib@rosa_pipeline/CE-811') _

if (env.JENKINS_URL.contains('lzdev') || env.JENKINS_URL.contains('lzprod')) {
    main_pipeline(
        Upper_Environemnts: ["staging","prod","demo","training", "produk"],
        Lower_Environemnts: ["master","apple","orange","blue","perf","int1","int2","int3","int4","int5","int6","int7","int8"],
        ALB_PREFIX: "cpwebalb",
    )
} else {
    rosa_pipeline(
        Rosa_Lower_Environments: ["us-gov-dev"],
        Rosa_Upper_Environments: ["us-gov-prod"],
        Repo_name: "ae-citizenportalweb"
    )
}
