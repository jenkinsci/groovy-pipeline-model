stage('build') {
    checkout scm
    sh 'mvn install'
    stash id:'zot', path:'xxx'
}
stage('test') {
    parallel(
        test1: {
            sh './test.sh 1'
        },
        test2: {
            sh './test.sh 2'
            sh './teardown.sh'
        }
    )
}
stage('deploy') {
    heroku app:'foo/bar'
}
