package com.haverzard.workitout.util

class CustomTime {
    var hours: Int
        set(value) {
            field = value
            time = this.toMillis()
        }
    var minutes: Int
        set(value) {
            field = value
            time = this.toMillis()
        }
    var seconds: Int
        set(value) {
            field = value
            time = this.toMillis()
        }
    var time: Long = 0
        private set

    constructor(hours: Int, minutes: Int, seconds: Int) {
        this.hours = hours
        this.minutes = minutes
        this.seconds = seconds
        time = this.toMillis()
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
        result = 31 * result + time.hashCode()
        return result
    }
}