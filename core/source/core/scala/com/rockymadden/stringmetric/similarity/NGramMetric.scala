package com.rockymadden.stringmetric.similarity

import com.rockymadden.stringmetric.{ FilterableConfigurableStringMetric, MatchTuple, StringFilter, StringMetric }
import scala.math

/** An implementation of the N-Gram [[com.rockymadden.stringmetric.StringMetric]]. */
object NGramMetric extends StringMetric with FilterableConfigurableStringMetric[Int] {
	type CompareReturn = Double

	override def compare(charArray1: Array[Char], charArray2: Array[Char])(n: Int)
		(implicit stringFilter: StringFilter): Option[CompareReturn] = {

		if (n <= 0) throw new IllegalArgumentException("Expected valid n.")

		val fca1 = stringFilter.filter(charArray1)
		lazy val fca2 = stringFilter.filter(charArray2)

		if (fca1.length < n || fca2.length < n) None // Because length is less than n, it is not possible to compare.
		else if (fca1.sameElements(fca2)) Some(1d)
		else
			NGramAlgorithm.compute(fca1)(n).flatMap { ca1bg =>
				NGramAlgorithm.compute(fca2)(n).map { ca2bg =>
					val ms = scoreMatches((ca1bg.map(_.mkString), ca2bg.map(_.mkString)))

					ms.toDouble / math.max(ca1bg.length, ca2bg.length)
				}
			}
	}

	override def compare(string1: String, string2: String)(n: Int)
		(implicit stringFilter: StringFilter): Option[CompareReturn] =

		compare(
			stringFilter.filter(string1.toCharArray),
			stringFilter.filter(string2.toCharArray)
		)(n)

	private[this] def scoreMatches(mt: MatchTuple[String]) = mt._1.intersect(mt._2).length
}