package com.acxiom.pipeline.steps

import com.acxiom.pipeline.annotations.{StepFunction, StepParameter}
import org.apache.log4j.Logger
import org.apache.spark.sql.DataFrame
import java.util.UUID.randomUUID

import com.acxiom.pipeline.PipelineContext

object QuerySteps {
  private val logger = Logger.getLogger(getClass)

  /**
    * Save an existing dataframe to a TempView
    * @param dataFrame        the dataframe to store
    * @param viewName         the name of the view to created (optional, random name will be created if not provided)
    * @param pipelineContext  the pipeline context
    * @return   the name of the TempView created that can be used in future queries for this session
    */
  @StepFunction(
    "541c4f7d-3524-4d53-bbd9-9f2cfd9d1bd1",
    "Save a Dataframe to a TempView",
    "This step stores an existing dataframe to a TempView to be used in future queries in the session",
    "Pipeline"
  )
  def dataFrameToTempView(dataFrame: DataFrame, viewName: Option[String], pipelineContext: PipelineContext): String = {
    val outputViewName = if(viewName.isEmpty) generateTempViewName else viewName.get
    logger.info(s"storing dataframe to tempView '$outputViewName")
    dataFrame.createOrReplaceTempView(outputViewName)
    outputViewName
  }

  /**
    * Run a query against existing TempViews from this session and return another TempView
    * @param query            the query to run (all tables referenced must exist as TempViews created in this session)
    * @param variableMap      the key/value pairs to be used in variable replacement in the query
    * @param viewName         the name of the view to created (optional, random name will be created if not provided)
    * @param pipelineContext  the pipeline context
    * @return   the name of the TempView created that can be used in future queries for this session
    */
  @StepFunction(
    "71b71ef3-eaa7-4a1f-b3f3-603a1a54846d",
    "Create a TempView from a Query",
    "This step runs a SQL statement against existing TempViews from this session and returns a new TempView",
    "Pipeline"
  )
  def queryToTempView(@StepParameter(typeOverride = Some("script"), language = Some("sql")) query: String, variableMap: Option[Map[String, String]],
                      viewName: Option[String], pipelineContext: PipelineContext): String = {
    val outputViewName = if(viewName.isEmpty) generateTempViewName else viewName.get
    logger.info(s"storing dataframe to tempView '$outputViewName")
    queryToDataFrame(query, variableMap, pipelineContext).createOrReplaceTempView(outputViewName)
    outputViewName
  }

  /**
    * Create a dataframe from a query
    * @param query            the query to run (all tables referenced must exist as TempViews created in this session)
    * @param variableMap      the key/value pairs to be used in variable replacement in the query
    * @param pipelineContext  the pipeline context
    * @return   a new DataFrame resulting from the query provided
    */
  @StepFunction(
    "61378ed6-8a4f-4e6d-9c92-6863c9503a54",
    "Create a DataFrame from a Query",
    "This step runs a SQL statement against existing TempViews from this session and returns a new DataFrame",
    "Pipeline"
  )
  def queryToDataFrame(@StepParameter(typeOverride = Some("script"), language = Some("sql")) query: String,
                       variableMap: Option[Map[String, String]], pipelineContext: PipelineContext): DataFrame = {
    val finalQuery = replaceQueryVariables(query, variableMap)
    // return the dataframe
    pipelineContext.sparkSession.get.sql(finalQuery)
  }

  /** replace runtime variables in a query string
    *
    * @param query  the query with the variables that need to be replaced
    * @param variableMap    the key value pairs that will be used in the replacement
    * @return  a new query string with all variables replaced
    */
  private[steps] def replaceQueryVariables(query: String, variableMap: Option[Map[String, String]]): String = {
    logger.debug(s"query before variable replacement")
    val finalQuery = if(variableMap.isEmpty) {
      // all variables have been replaced, now run standard replacements
      query.replaceAll(";", "")
    } else {
      variableMap.get.foldLeft(query)( (tempQuery, variable) => {
        tempQuery.replaceAll("\\$\\{" + variable._1 + "\\}", variable._2)
          .replaceAll(";", "")
      })
    }
    logger.debug(s"query after variable replacement=$finalQuery")
    validateQuery(finalQuery)
    finalQuery
  }

  /**
    * log any isses found after variable replacment is performed on a query
    * @param query  the query to validate
    */
  private def validateQuery(query: String): Unit = {
    // check for variable identifiers and log warning if they exist
    if(query.contains("${")) {
      logger.warn(s"variable identifiers found after replacement,query=$query")
    }
    // TODO: add other validations??
  }

  // generate a unique table name when no name is provided
  private[steps] def generateTempViewName: String = s"t${randomUUID().toString.replace("-","")}"

}