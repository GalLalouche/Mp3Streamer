/** `any` by any other name. It has all fields, but has to be explicit cast other types. */
export interface RawJSON {
  [key: string]: any
}

/**
 * An intentionally opaque wrapper around jQuery's `$.get`, so clients are forced to either
 * explicitly typecast or use a parse method.
 */
export async function get(path: string): Promise<RawJSON> {
  return $.get(path).toPromise()
}
