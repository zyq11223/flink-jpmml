/*
 * flink-jpmml
 * Copyright (C) 2017 Radicalbit

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.flink.pmml.scala.api.functions

import io.radicalbit.flink.pmml.scala.ModelLoadingException
import io.radicalbit.flink.pmml.scala.api.PmmlModel
import io.radicalbit.flink.pmml.scala.api.reader.ModelReader
import io.radicalbit.flink.pmml.scala.logging.LazyLogging
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration

import scala.util.{Failure, Success, Try}

/** Abstract class extending a [[RichFlatMapFunction]]; it provides:
  * the `evaluator` lazy evaluated object as instance of [[PmmlModel]]
  * the open method as a builder of the evaluator instance at operator initialization time
  *
  * @param reader
  * @tparam IN
  * @tparam OUT
  */
private[scala] abstract class EvaluationFunction[IN, OUT](reader: ModelReader)
    extends RichFlatMapFunction[IN, OUT]
    with LazyLogging {

  protected lazy val evaluator: PmmlModel = PmmlModel.fromReader(reader)

  /** initializes the evaluator object
    *
    * @param parameters [[Configuration]]
    * @throws ModelLoadingException if the model could not be initialized at operator construction time
    */
  override def open(parameters: Configuration): Unit = {
    Try(evaluator.evaluator.getModel) match {
      case Success(model) => logger.info(s"Model has been read successfully, model name: ${model.getModelName}")
      case Failure(e) => throw new ModelLoadingException(e.getMessage, e)
    }
  }

}
