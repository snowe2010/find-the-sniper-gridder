require 'json'

task :setup do 
  puts "installing cdk"
  system "echo 'hi'"
  system "npm install aws-cdk"
end

task :deploy do
  if ENV['CI']
    puts "Deploying"
    system "npx cdk deploy"
  end
end

task :localstack do
  system %Q(docker-compose up -d)
end

task add_secrets: [:localstack]  do
  secrets = JSON.parse(File.open(".secrets.json"))
  system %Q(aws --endpoint-url=http://localhost:4566 secretsmanager create-secret --name findthesniper-secrets --secret-string '{"imgurClientId":"#{secrets["imgurClientId"]}","redditClientId":"#{secrets["redditClientId"]}","redditClientSecret":"#{secrets["redditClientSecret"]}","redditUsername":"#{secrets["redditUsername"]}","redditPassword":"#{secrets["redditPassword"]}"}')
  system %Q(aws --endpoint-url=http://localhost:4566 secretsmanager update-secret --secret-id findthesniper-secrets --secret-string '{"imgurClientId":"#{secrets["imgurClientId"]}","redditClientId":"#{secrets["redditClientId"]}","redditClientSecret":"#{secrets["redditClientSecret"]}","redditUsername":"#{secrets["redditUsername"]}","redditPassword":"#{secrets["redditPassword"]}"}')
end

task :load_secrets do
  puts JSON.parse(IO.read(".secrets.json"))
end
