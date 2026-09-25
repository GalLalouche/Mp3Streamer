/** `any` by any other name */
export interface RawJSON {
  [key: string]: any;
}

/**
 * An intentionally opaque wrapper around jQuery's `$.get`, so clients are forced to either
 * explicitly typecast or use a parse method.
 */
export async function get(path: string): Promise<RawJSON> {
  return $.get(path).toPromise()
}
