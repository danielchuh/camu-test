pipeline {
    agent any

    // environment {
    //     // Define environment variables if needed
    // }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the code from GitHub
                git branch: 'develop', credentialsId: 'daniel-akieni', url: 'https://github.com/openimis/openimis-be_py.git'
            }
        }
        stage('Install Dependencies') {
            steps {
                // Install pip-audit
                sh 'pip install --upgrade pip pip-audit'
                sh 'pip install pip-audit'
                // Install pyscan
                sh 'pip install pyscan python-nmap'
                // Install bandit
                sh 'pip install bandit'
                // Install prospector and related tools prospector-html-reporter
                sh 'pip install prospector prospector2html'
                // Install mypy 
                sh 'pip install mypy'
            }
        }
        stage('Run pip-audit') {
            steps {
                // Run pip-audit and generate report
                sh 'pip-audit --requirement requirements.txt -f json -o report.json'
            }
        }
        stage('Run PyScan') {
            steps {
                // Run pyscan (not specifying report generation since it wasn't found)
                sh 'pyscan'
            }
        }
        stage('Run Bandit') {
            steps {
                // Run bandit and generate HTML report
                sh 'bandit -r . -lll -f html -o bandit_report.html'
            }
        }
        stage('Run Prospector') {
            steps {
                // Run prospector and generate JSON report, then convert to HTML
                sh 'prospector --no-style-warnings --strictness medium --output-format json > prospector_report.json'
                // sh 'prospector-html --input prospector_report.json'
            }
        }
        stage('Run MyPy') {
            steps {
                // Create mypy.ini file
                writeFile file: 'mypy.ini', text: '''
[mypy]
ignore_missing_imports = True
ignore_errors = True
disallow_untyped_defs = True
disallow_untyped_calls = True

[mypy-module.*]
ignore_errors = False

[mypy-module.migrations.*]
ignore_errors = True
                '''
                // Run mypy
                sh 'mypy .'
            }
        }
    }
    post {
        always {
            // Archive the generated reports
            archiveArtifacts artifacts: '*.html, *.json', allowEmptyArchive: true
        }
        cleanup {
            // Clean up workspace if necessary
            cleanWs()
        }
    }
}
