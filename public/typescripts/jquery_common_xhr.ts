export {}

declare global {
  namespace JQuery {
    interface jqXHR<TResolve = any> {
      toPromise(): globalThis.Promise<TResolve>
    }
  }
}

$.ajaxPrefilter(function (options, originalOptions, jqXHR) {
  (jqXHR as JQuery.jqXHR).toPromise = function (this: JQuery.jqXHR) {
    return new Promise<any>((resolve, reject) => {
      this.then(
        (data: any) => resolve(data),
        (jqXHR, textStatus, errorThrown) => reject(errorThrown),
      )
    }) as any
  }
})

