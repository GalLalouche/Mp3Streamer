String.prototype.takeAfterLast = function(subs) {
  return this.substr(this.lastIndexOf(subs) + 1)
}
Boolean.prototype.isFalse = function() {
  return not(this.valueOf())
}

Number.prototype.timeFormat = function() {
  let hours = Math.floor(this / 3600)
  let minutes = Math.floor((this - (hours * 3600)) / 60)
  let seconds = Math.round(this - (hours * 3600) - (minutes * 60))

  hours = (0 < hours && hours < 10 ? "0" : "") + hours
  if (hours < 10) hours = "0" + hours
  if (minutes < 10) minutes = "0" + minutes
  if (seconds < 10) seconds = "0" + seconds

  const hourPrefix = hours === "00" ? "" : hours + ":"
  return hourPrefix + minutes + ':' + seconds
}

const ENTER_CODE = 13
Array.prototype.custom_last = function() {
  return this[this.length - 1]
}
Array.prototype.custom_sort_by = function(f) {
  return this
      .map(e => [e, f(e)])
      .sort((a, b) => {
        const fa = a[1]
        const fb = b[1]
        if (fa === fb)
          return 0
        if (fa < fb)
          return -1
        else
          return 1
      })
      .map(e => e[0])
}
Array.prototype.custom_group_by = function(f) {
  let result = {}
  this.forEach(e => {
    const key = f(e)
    if (result[key] === undefined)
      result[key] = []
    result[key].push(e)
  })
  return result
}
Array.prototype.custom_max = function() {
  let max = null
  for (const e of this) {
    if (max === null) {
      max = e
    } else {
      max = max > e ? max : e
    }
  }
  return max
}

function assert(condition, message) {
  if (condition)
    return
  if (not(message))
    return assert(condition, "Assertion failed")
  throw typeof Error === "undefined" ? message : new Error(message)
}

class AssertionError extends Error {
  constructor(message) {
    super(message || "AssertionError")
  }
}