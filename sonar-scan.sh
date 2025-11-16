# Get the current branch name directly using command substitution
CURRENT_BRANCH=$(git branch --show-current)

# Check if the command was successful and a branch name was found
if [ -z "$CURRENT_BRANCH" ]; then
    echo "Error: Could not determine the current Git branch."
    exit 1
fi

mvn clean verify sonar:sonar \
  -Dsonar.projectKey=e-corp-demo_JavaCoffeeShop_9f714693-bd2c-4c91-af82-9fc65311d0e0 \
  -Dsonar.projectName='JavaCoffeeShop' \
  -Dsonar.host.url=https://santoshsqs.ngrok.app \
  -Dsonar.branch.name=$CURRENT_BRANCH
