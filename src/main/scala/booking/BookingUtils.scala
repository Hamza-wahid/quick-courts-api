package booking

import java.time.{LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

object BookingUtils {

  private val timeFormatter  = DateTimeFormatter.ofPattern("HH:mm")
  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

  def extractDate(dateTimeString: String): String = {
    convertToLocalDateTime(dateTimeString)
      .toLocalDate
      .toString
  }

  def extractTime(dateTimeString: String): String = {
    convertToLocalDateTime(dateTimeString)
      .toLocalTime
      .toString
  }

  def convertToLocalDateTime(dateTimeString: String): LocalDateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter)

  implicit def convertToLocalTime(timeString: String): LocalTime = LocalTime.parse(timeString, timeFormatter)


}
