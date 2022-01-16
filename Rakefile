require 'json'

task :setup do 
  puts "installing cdk"
  system "npm install aws-cdk"
end

task :cdk_bootstrap do
  secrets = JSON.parse(File.open(".secrets.json"))
  system "npx cdk bootstrap aws://#{secrets["awsAccountId"]}/us-west-1"
end

task :build do
  system "./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true"
end

task deploy: [:build] do
  # if ENV['CI']
  puts "Deploying"
  system "npx cdk deploy"
  # end
end

task :bootstrap_localstack do
  system "npx cdklocal bootstrap aws://000000000000/us-west-1 --profile localstack"
end

task :deploy_localstack do 
  system "npx cdklocal deploy -v"
end

task :localstack do
  system %Q(docker-compose up -d)
end

task add_secrets: [:localstack]  do
  secrets = JSON.parse(File.open(".secrets.json"))
  system %Q(aws --endpoint-url=http://localhost:4566 secretsmanager create-secret --name findthesniper-secrets --secret-string '{"imgurClientId":"#{secrets["imgurClientId"]}","redditClientId":"#{secrets["redditClientId"]}","redditClientSecret":"#{secrets["redditClientSecret"]}","redditUsername":"#{secrets["redditUsername"]}","redditPassword":"#{secrets["redditPassword"]}"}')
  # system %Q(aws --endpoint-url=http://localhost:4566 secretsmanager update-secret --secret-id findthesniper-secrets --secret-string '{"imgurClientId":"#{secrets["imgurClientId"]}","redditClientId":"#{secrets["redditClientId"]}","redditClientSecret":"#{secrets["redditClientSecret"]}","redditUsername":"#{secrets["redditUsername"]}","redditPassword":"#{secrets["redditPassword"]}"}')
end


task :load_secrets do
  puts JSON.parse(IO.read(".secrets.json"))
end
