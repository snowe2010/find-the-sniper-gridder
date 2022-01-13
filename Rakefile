
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
