require 'json'

task :setup do 
  puts "installing cdk"
  system "npm install aws-cdk"
  system "npm install aws-cdk-local"
end

task :cdk_bootstrap do
  secrets = load_secrets
  system "npx cdk bootstrap aws://#{secrets["awsAccountId"]}/us-west-1"
end

task :build do
  system "./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true"
end

task deploy: [:build] do
  puts "Deploying"
  system "npx cdk deploy"
end

task :bootstrap_localstack do
  system "npx cdklocal bootstrap aws://000000000000/us-west-1 --profile localstack"
end

task :deploy_localstack do 
  system "npx cdklocal deploy -v"
end

task :localstack do
  system %Q(docker-compose up -d)
  sleep 10
end

task add_secrets: [:localstack]  do
  secrets = load_secrets
  system %Q(awslocal secretsmanager create-secret --region us-west-1 --name findthesniper-secrets --secret-string '{"imgurClientId":"#{secrets["imgurClientId"]}","redditClientId":"#{secrets["redditClientId"]}","redditClientSecret":"#{secrets["redditClientSecret"]}","redditUsername":"#{secrets["redditUsername"]}","redditPassword":"#{secrets["redditPassword"]}"}')
  # system %Q(aws --endpoint-url=http://localhost:4566 secretsmanager update-secret --secret-id findthesniper-secrets --secret-string '{"imgurClientId":"#{secrets["imgurClientId"]}","redditClientId":"#{secrets["redditClientId"]}","redditClientSecret":"#{secrets["redditClientSecret"]}","redditUsername":"#{secrets["redditUsername"]}","redditPassword":"#{secrets["redditPassword"]}"}')
end

task setup_dynamo_db: [:localstack] do
  system %Q(awslocal dynamodb create-table --region us-west-1 --table-name find-the-sniper-helper-RedditParsedPosts --attribute-definitions AttributeName=id,AttributeType=S AttributeName=ttl,AttributeType=N --key-schema AttributeName=id,KeyType=HASH AttributeName=ttl,KeyType=RANGE --provisioned-throughput ReadCapacityUnits=10,WriteCapacityUnits=5)
end

task start_localstack: [:setup_dynamo_db, :add_secrets, :localstack]

def load_secrets
  JSON.parse(IO.read(".secrets.json"))
end
