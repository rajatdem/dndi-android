#!/bin/bash -e

main(){
    
    case $1 in
        "coverage")
            echo coverage
            ./gradlew clean jacocoTestReport
            open app/build/reports/jacoco/jacocoTestReport/html/index.html
            ;;
        *)
            usage
            ;;
    esac
}

usage(){
    printf "\nUsage: ./run.sh <command>\n\n"
    printf "coverage                   generate html coverage report\n"
    printf "\n"
    
}

main "$@"

