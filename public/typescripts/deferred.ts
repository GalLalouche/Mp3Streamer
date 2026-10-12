/** Analogue to Scala's Promise. */
export class Deferred<T> {
  readonly promise: Promise<T> = new Promise<T>((res, rej) => {
    this.res = res
    this.rej = rej
  })

  private res!: (value: T | PromiseLike<T>) => void
  private rej!: (reason?: unknown) => void
  private isClosed: boolean = false

  resolve(value: T | PromiseLike<T>): void {
    this.close()
    this.res(value)
  }

  reject(reason?: unknown): void {
    this.close()
    this.rej(reason)
  }

  private close(): void {
    if (this.isClosed)
      throw new Error("Deferred has already been resolved or rejected.")
    this.isClosed = true
  }
}
