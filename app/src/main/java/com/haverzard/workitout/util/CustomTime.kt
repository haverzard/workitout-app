package com.haverzard.workitout.util

class CustomTime {
    var hours: Int
        set(value) {
            field = value
            time = this.toMillis()
            timeInSeconds = this.toSeconds()
        }
    var minutes: Int
        set(value) {
            field = value
            time = this.toMillis()
            timeInSeconds = this.toSeconds()
        }
    var seconds: Int
        set(value) {
            field = value
            time = this.toMillis()
            timeInSeconds = this.toSeconds()
        }
    var timeInSeconds: Int = 0
        private set
    var time: Long = 0
        private set

    constructor(hours: Int, minutes: Int, seconds: Int) {
        this.hours = hours
        this.minutes = minutes
        this.seconds = seconds
        time = this.toMillis()
        timeInSeconds = this.toSeconds()
    }

    private fun toSeconds(): Int {
        return hours * 3600 + minutes * 60 + seconds
    }

    private fun toMillis(): Long {
        return (hours * 3600L + minutes * 60L + seconds) * 1000L
    }

    override fun equals(other: Any?): Boolean {
        if (other is CustomTime) {
            return (hours == other.hours) && (minutes == other.minutes) && (seconds == other.seconds)
        }
        return super.equals(other)
    }

    operator fun compareTo(other: Any?): Int {
        if (other is CustomTime) {
            val greater = hours > other.hours || (hours == other.hours &&
                (minutes > other.minutes ||
                    minutes == other.minutes && seconds > other.seconds
                )
            )
            if (greater) {
                return 1
            } else if (this == other) {
                return 0
            }
            return -1
        }
        return 0
    }

    override fun hashCode(): Int {
        var result = hours
        result = 31 * result + minutes
        result = 31 * result + seconds
        result = 31 * result + timeInSeconds
        result = 31 * result + time.hashCode()
        return result
    }

    companion object {
        fun fromSeconds(_seconds: Int): CustomTime {
            var seconds = _seconds
            val hours = seconds / 3600
            seconds %= 3600
            val minutes = seconds / 60
            seconds %= 60
            return CustomTime(hours, minutes, seconds)
        }
    }
}