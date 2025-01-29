export {}

declare global {
  namespace JQuery {
    interface jqXHR<TResolve = any> {
      void(): Promise<void>
    }
  }
}

$.ajaxPrefilter(function (options, originalOptions, jqXHR) {
  (jqXHR as JQuery.jqXHR).void = function (this: JQuery.jqXHR) {
    return this.then(() => {})
  }
})
