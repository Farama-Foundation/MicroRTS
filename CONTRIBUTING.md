# Contributing

Thanks for taking an interest in microRTS!

When contributing to this repository, it is a good idea to discuss the change you wish to make via an issue or an early pull request in order to keep everyone informed of ongoing work, as well as to make sure that there is interest in the change.

## How to make a pull request

1. Fork this repository and create a new branch on your fork from the master branch. Do all your work in that fork.
2. If you plan to make multiple unrelated contributions, separate each one in a different fork.
3. After implementing your changes, start a pull request from the custom branch of your repository to the master branch of this repository.

## Tips on maintaining the quality of the project

Please, follow these steps if you want to raise the chances of having your pull request accepted with as few corrections as possible:

1. Refrain from including changes that are unnecessary or unrelated with the contribution, or from changing to many files in a single pull request, as this raises the probability of creating breaking changes with the work of others and making manual merges necessary.
2. After creating a pull request, automated tests will be performed in order to make sure the contributed code compiles successfully. If it does not, we ask you to fix the compilation before the pull request is merged, as the master branch is expected to work out of the box.
3. In order to minimize unrelated changes to the code and maximize quality and readability, we ask contributors to format all source code files to comply with the code standard of the repository. We use [uncrustify](https://github.com/uncrustify/uncrustify) as an IDE-independent formatter. A configuration file is provided. Here is an example on how to execute uncrustify in all Java files inside the current directory, via a terminal:

```sh
uncrustify -c .uncrustify --replace $(find src -type f -name "*.java")
```