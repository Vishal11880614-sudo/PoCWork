// Databricks notebook source
val containerName = "mynewcontainer"
val storageAccountName = "myfirststorageusaeast"
val sas = "?sv=2019-10-10&ss=b&srt=sco&sp=rwdlacx&se=2020-07-31T15:07:03Z&st=2020-07-02T07:07:03Z&spr=https&sig=TYf099AH%2FNjjAdOVZbhw7k65sKgNq1VkjEa4XGkyusY%3D"
val config = "fs.azure.sas." + containerName+ "." + storageAccountName + ".blob.core.windows.net"

// COMMAND ----------

dbutils.fs.unmount("/mnt/adbdata")

// COMMAND ----------

dbutils.fs.mount(
  source = "wasbs://mynewcontainer@myfirststorageusaeast.blob.core.windows.net/",
  mountPoint = "/mnt/adbdata",
  extraConfigs = Map(config -> sas))

// COMMAND ----------

import java.io._
import org.apache.spark.sql.functions._
val filename = dbutils.widgets.get("Infilename")
var path = "/dbfs/mnt/adbdata/"+ dbutils.widgets.get("RunId") + ".csv" ;
val datafactoryname = dbutils.widgets.get("dfname")
val pipelinename = dbutils.widgets.get("pplname")
val pipelineRunId = dbutils.widgets.get("RunId")


// COMMAND ----------

try {
  val mydf = spark.read
  .option("header","true")
  .option("inferSchema", "true")
  .csv("/mnt/adbdata/1000_Sales_Records.csv")
  val selectspecificcolsdf = mydf.select("Region", "Country", "SalesChannel","OrderPriority","TotalProfit")
  val renamedColsmyDF = selectspecificcolsdf.withColumnRenamed("SalesChannel","SalesPlatform")
  renamedColsmyDF.write
  .option("header", "true")
  .format("com.databricks.spark.csv")
  .save("/mnt/adbdata/" +filename)
  
  val pw = new PrintWriter(new File(path))
  pw.write("DatafactoryName,Pipelinename,Runid,ErrorMessage");
  pw.write("\n");
  pw.write(datafactoryname + "," + pipelinename + "," + pipelineRunId + "," + "Transformation completed Successfully!");
  pw.close
  println("Transformation completed Successfully!");
  
} catch {
  case e: Exception => val valerr = e
  
  val pw = new PrintWriter(new File(path))
  pw.write("DatafactoryName,Pipelinename,Runid,ErrorMessage");
  pw.write("\n");
  pw.write(datafactoryname + "," + pipelinename + "," + pipelineRunId + "," + "exception caught: " + valerr);
  pw.close
  println("exception caught: " + valerr);
  
}
