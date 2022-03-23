package com.rijal.salesandcredit.helpers

import java.util.*

class DateUtil {
    companion object {
        fun firstDayInMonth(): Date {
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)

            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE))
            return cal.time
        }
        fun lastDayInMonth(): Date {
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR, cal.getActualMaximum(Calendar.HOUR))
            cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE))
            return cal.time
        }
        fun firstDayInYear(): Date {
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)

            cal.set(Calendar.MONTH, 0)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE))
            return cal.time
        }
        fun lastDayInYear(): Date {
            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)

            cal.set(Calendar.MONTH, 11)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR, cal.getActualMaximum(Calendar.HOUR))
            cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE))
            return cal.time
        }
    }
}