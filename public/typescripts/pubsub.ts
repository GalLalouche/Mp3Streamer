/** A generic pubsub system. Uncoupled to any one system or another. */
// FIXME this was copy pasted from an agent with A LOT of extra explanation. Clean it up.
declare const payloadType: unique symbol
// `unique symbol` means this is the type of one specific symbol,
// so it can identify one specific property in a type.

export type Topic<T> = {
  readonly key: symbol
  // `symbol` is the type of a runtime symbol value.
  // `Symbol(name)` below creates that value for the bus to use.

  readonly [payloadType]: (value: T) => T
  // `[payloadType]` names a property using the special symbol above.
  // This property is a compile-time marker tying this Topic to T.
  // It isn't actually added to the runtime object.
  // `(value: T) => T` means “a function from T to T.”
}

export function topic<T>(name: string): Topic<T> {
  return {key: Symbol(name)} as Topic<T>
  // `<T>` lets the caller choose the topic's payload type.
  // `Symbol(name)` creates a unique runtime key; `name` is just a label.
  // `as Topic<T>` tells TypeScript to trust that this object is a Topic<T>,
  // even though it doesn't have the compile-time-only marker property.
}

export class PubSub {
  private readonly listeners = new Map<symbol, Set<(value: any) => void>>()
  // `Map` looks up listeners by the topic's runtime symbol.
  // `Set` holds the callbacks for one topic.
  // `any` lets this one map hold callbacks for different payload types.
  // The public methods below still check payload types.

  constructor() {}

  listen<T>(
    topic: Topic<T>,
    f: (value: T) => void
  ): () => void {
    // `<T>` declares a type variable for this method call.
    // TypeScript infers T from the topic argument.

    let bucket = this.listeners.get(topic.key)

    if (!bucket) {
      bucket = new Set()
      this.listeners.set(topic.key, bucket)
    }

    bucket.add(f)

    return () => bucket!.delete(f)
    // `!` tells TypeScript that bucket isn't undefined here.
    // The returned function removes this listener.
  }

  unlisten<T>(
    topic: Topic<T>,
    f: (value: T) => void
  ): void {this.listeners.get(topic.key)?.delete(f)}

  publish<T>(topic: Topic<T>, value: T): void {
    // This T is inferred from the topic too, so value must match it.
    for (const f of this.listeners.get(topic.key) ?? []) {
      // `?? []` uses an empty list if this topic has no listeners.
      f(value)
    }
  }
}