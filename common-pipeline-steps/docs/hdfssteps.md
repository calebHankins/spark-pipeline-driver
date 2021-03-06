# HDFSSteps
This step object provides a way to read from and write to HDFS. There are two step functions provided:

## Write to Path
This function will write a given DataFrame to the provided path. Full parameter descriptions are listed below:

* **dataFrame** - A dataFrame to be written to HDFS.
* **path** - A HDFS path to write to.
* **options** - Optional DataFrameWriterOptions object to configure the DataFrameWriter

## Read From Path
This function will read a file from the provided path into a DataFrame. Full parameter descriptions are listed below:

* **path** - A HDFS file path to read.
* **options** - Optional DataFrameReaderOptions object to configure the DataFrameReader

## Read From Paths
This function will read from each of the provided paths into a DataFrame. Full parameter descriptions are listed below:

* **paths** - A list of HDFS file paths to read.
* **options** - Optional DataFrameReaderOptions object to configure the DataFrameReader
