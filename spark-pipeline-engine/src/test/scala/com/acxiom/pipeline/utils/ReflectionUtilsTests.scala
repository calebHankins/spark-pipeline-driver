package com.acxiom.pipeline.utils

import com.acxiom.pipeline.{PipelineStepResponse, _}
import org.scalatest.FunSpec

class ReflectionUtilsTests extends FunSpec {
  describe("ReflectionUtil - processStep") {
    it("Should process step function") {
      val pipelineContext = PipelineContext(None, None, None, PipelineSecurityManager(), PipelineParameters(),
        Some(List("com.acxiom.pipeline.steps", "com.acxiom.pipeline")), PipelineStepMapper(), None, None)
      val step = PipelineStep(None, None, None, None, None, Some(EngineMeta(Some("MockStepObject.mockStepFunction"))))
      val response = ReflectionUtils.processStep(step,
        Map[String, Any]("string" -> "string", "boolean" -> true), pipelineContext)
      assert(response.isInstanceOf[PipelineStepResponse])
      assert(response.asInstanceOf[PipelineStepResponse].primaryReturn.isDefined)
      assert(response.asInstanceOf[PipelineStepResponse].primaryReturn.getOrElse("") == "string")
      assert(response.asInstanceOf[PipelineStepResponse].namedReturns.isDefined)
      assert(response.asInstanceOf[PipelineStepResponse].namedReturns.get("boolean").asInstanceOf[Boolean])
    }

    it("Should process step function with non-PipelineStepResponse") {
      val pipelineContext = PipelineContext(None, None, None, PipelineSecurityManager(), PipelineParameters(),
        Some(List("com.acxiom.pipeline.steps", "com.acxiom.pipeline")), PipelineStepMapper(), None, None)
      val step = PipelineStep(None, None, None, None, None,
        Some(EngineMeta(Some("MockStepObject.mockStepFunctionAnyResponse"))))
      val response = ReflectionUtils.processStep(step, Map[String, Any]("string" -> "string"), pipelineContext)
      assert(response.isInstanceOf[PipelineStepResponse])
      assert(response.asInstanceOf[PipelineStepResponse].primaryReturn.isDefined)
      assert(response.asInstanceOf[PipelineStepResponse].primaryReturn.getOrElse("") == "string")
    }

    it("Should instantiate a class") {
      val className = "com.acxiom.pipeline.MockClass"
      val mc = ReflectionUtils.loadClass(className, Some(Map[String, Any]("string" -> "my_string")))
      assert(mc.isInstanceOf[MockClass])
      assert(mc.asInstanceOf[MockClass].string == "my_string")
    }

    it("Should instantiate a complex class") {
      val className = "com.acxiom.pipeline.MockDriverSetup"
      val params = Map[String, Any]("parameters" -> Map[String, Any]("initialPipelineId" -> "pipelineId1"))
      val mockDriverSetup = ReflectionUtils.loadClass(className, Some(params))
      assert(mockDriverSetup.isInstanceOf[MockDriverSetup])
      assert(mockDriverSetup.asInstanceOf[MockDriverSetup].initialPipelineId == "pipelineId1")
    }

    it("Should instantiate no param constructor") {
      val className = "com.acxiom.pipeline.MockNoParams"
      val mc = ReflectionUtils.loadClass(className, None)
      assert(mc.isInstanceOf[MockNoParams])
      assert(mc.asInstanceOf[MockNoParams].string == "no-constructor-string")
    }
  }
}