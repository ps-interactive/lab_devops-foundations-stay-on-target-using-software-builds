pipeline {
    agent any

    stages {
        stage('Version Control') {
            steps {
                checkout changelog: false, poll: false, scm: scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'file:////home/code']])
            }
        }
        stage('Build') {
            steps {
                sh '/var/jenkins_home/tools/io.jenkins.plugins.dotnet.DotNetSDK/.NET/dotnet build Globomantics.Intranet.Web --no-restore'
            }
        }
        stage('Test') {
            steps {
                sh '/var/jenkins_home/tools/io.jenkins.plugins.dotnet.DotNetSDK/.NET/dotnet test Globomantics.Intranet.Web --no-restore'
            }
        }
    }
}
