# The Oscii Lexicon

A research project to learn, index, and display all lexical information.

## Building

* Check out the `master` branch of https://github.com/stanfordnlp/CoreNLP
* Check out the `suffix-array` branch of https://github.com/stanfordnlp/phrasal
* Set environment variables (e.g., add these lines to your `.bashrc`):
    - `export CORENLP_HOME=/path/to/CoreNLP`
    - `export PHRASAL_HOME=/path/to.phrasal`
* Run `gradle test` to build and test `lex`

## Getting started

The lexicon uses [PanLex](http://panlex.org/) data as a seed. To run the
system,
* [download](http://dev.panlex.org/db/) a `JSON`-formatted archive of PanLex data.
* To compile a lexicon as a `lex.json` file, execute `gradle run -Pargs="-p /path/to/panlex/directory -w /path/to/lex.json"`
* To serve translations using rabbitmq, execute `gradle run -Pargs="-r /path/to/lex.json -s"`

