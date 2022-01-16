# Find the Sniper Gridder

Adds coords and grids to /r/findthesniper images

# setup

## prerequisites

You'll need [asdf](https://asdf-vm.com/) with the `java`, `nodejs`, and `python` plugins. If you are running this on a 
platform that doesn't package `ruby` by default (Windows) then you'll also need to install that, any version should work. 

```shell
asdf plugin add java
asdf plugin add nodejs
asdf plugin add python
```

After doing the above simply run:

```shell
asdf install
``` 

to install all needed languages.

## localstack

This project uses localstack for local development. This means that you can start up AWS resources directly on your 
development machine using docker. To get started simply run:

```shell
rake setup
```

which will install the needed nodejs packages for interacting with AWS and localstack, namely `aws-cdk` and `aws-cdk-local`


## troubleshooting

You might need to reshim asdf after installing python packages or ruby gems. Run `asdf reshim python` or 
`asdf reshim ruby` if you encounter issues where a binary is unable to be found by your shell. 
