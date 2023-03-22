Evaluation framework for trace similarity metrics. Setup is tested on linux systems, but should be straightforward using utilities listed below.

## Prerequisites

- Maven
- Java 8+
- gunzip

## Setup

### Clone repo:

```git clone https://github.com/backco/tracesim.git```

### Extract event logs:

```
gunzip logs.tar.gz
tar -xf logs.tar
```

### Run evalumetric:

Note: argument order plays no role. Experiments will be run for the Cartesian product of event logs, similarity metrics, and evaluation measures. That is, for every log listed, every similarity metric listed will be evaluated with respect to every evaluation measure listed.

#### Run directly with Maven:

```mvn compile exec:java -Dexec.mainClass="com.co.back.evalumetric.EvalumetricMain" -Dexec.args="--input=<PATHS-TO-EVENT-LOGS> --output=<OUTPUT-FILE> <SIMILARITY-METRICS> <EVALUATION-MEASURES>"```

## Arguments

Note that evalumetric can write results directly to a MySQL database if credential are provided. Currently these must be set directly in code. It assumes the presence of a `results` table with the following schema: TODO

#### I/O

```
--input=<COMMA-SEPARATED-FILENAMES>     Event log to evaluate, should be absolute paths, comma separated
--output=<OUTPUT-FILE>                  Output file to write results to
--help                                  Print help prompt
```

#### Similarity metrics

```
--ngramcosine:n=<N>         n-gram embedding with cosine distance
--ngrameuclidean:n=<N>      n-gram embedding with Euclidean distance
--eventuallyfollowsdelias   Weighted eventually-follows due to Delias et. al
--mrcosine                  Maximal Repeat embedding with cosine distance
--smrcosine                 Super Maximal Repeat embedding with cosine distance
--nsmrcosine                Near Super Maximal Repeat embedding with cosine distance
--mreuclidean               Maximal Repeat embedding with Euclidean distance
--smreuclidean              Super Maximal Repeat embedding with Euclidean distance
--nsmreuclidean             Near Super Maximal Repeat embedding with Euclidean distance
--editlevenshtein           Levenshtein edit distance
--editlevenshteinnorm       Levenshtein distance normalized
--editgeneric               Generic edit distance (currently omitted due to licensing)
--alignmentnw               Alignment using Needleman-Wunsch algorithm - raw match count
--alignmentnwf1             Alignment using Needleman-Wunsch algorithm - F1 score
```

#### Evaluation measures

For all evaluation measures, `sample=` should be a value between 0.0 and 1.0.

```
--precisionatk:k=<K>,sample=<RATIO> Precision@K, set k=1 for nearest neighbor
--silhouette:sample=<RATIO>         Silhouette evaluation measure
--tripletbased:sample=<RATIO>       Triplet based evaluation measure
```


## Examples

Run all experiments for the Artificial Photo Copier Event Log. Sample ratio of 1.0 for all evaluation measures, i.e. sample the entire log, all possible triplets:

```mvn compile exec:java -Dexec.mainClass="com.co.back.evalumetric.EvalumetricMain" -Dexec.args="--input=/root/evalumetric/logs/Photocopier/Photocopier.xes --ngramcosine:n=1 --ngramcosine:n=2 --ngramcosine:n=3 --ngrameuclidean:n=1 --ngrameuclidean:n=2 --ngrameuclidean:n=3  --editlevenshtein --editlevenshteinnorm --alignmentnw --alignmentnwf1 --eventuallyfollowsdelias --mrcosine --mreuclidean --smrcosine --smreuclidean --nsmrcosine --nsmreuclidean --precisionatk:k=1,sample=1.0 --precisionatk:k=10,sample=1.0 --silhouette:sample=1.0 --tripletbased:sample=1.0 --output=output.txt"```


## Remarks

Implementation of generic edit distance has been omitted from this repository due to licensing restrictions, but can be found at:

Note that running all experiments with the sample ratios reported in the paper is very resource and time intensive.
