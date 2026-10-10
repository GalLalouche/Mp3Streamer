/* A generic pubsub system. Uncoupled to any one system or another. */
// FIXME this was copy pasted from an agent with A LOT of extra explanation. Clean it up.
declare const payloadType: unique symbol
// `unique symbol` means this is the type of one specific symbol,
// so it can identify one specific property in a type.

/** Typesafe marker for a pubsub topic. */
export type Topic<T> = {
  readonly key: symbol
  readonly [payloadType]: (value: T) => T
  // `[payloadType]` names a property using the special symbol above.
  // This property is a compile-time marker tying this Topic to T.
  // It isn't actually added to the runtime object.
  // `(value: T) => T` means “a function from T to T.”
}

/** Factory method for the above. */
export function topic<T>(name: string): Topic<T> {
  return {key: Symbol(name)} as Topic<T>
  // `Symbol(name)` creates a unique runtime key; `name` is just a label.
  // `as Topic<T>` tells TypeScript to trust that this object is a Topic<T>,
  // even though it doesn't have the compile-time-only marker property.
}

export interface Unsubscribe {
  unsubscribe(): void
}

/** A generic PubSub, supporting multiple topics and multiple listeners per topic. */
export class PubSub {
  private readonly listeners = new Map<symbol, Set<(value: any) => void>>()

  listen<T>(topic: Topic<T>, f: (t: T) => void): Unsubscribe {
    if (not(this.listeners.has(topic.key)))
      this.listeners.set(topic.key, new Set())
    const bucket = this.listeners.custom_get_or_throw(topic.key)
    bucket.add(f)
    return {unsubscribe: () => bucket.delete(f)}
  }

  unlisten<T>(topic: Topic<T>, f: (t: T) => void): void {
    this.listeners.get(topic.key)?.delete(f)
  }

  publish<T>(topic: Topic<T>, value: NoInfer<T>): void {
    for (const f of this.listeners.get(topic.key) ?? []) {
      f(value)
    }
  }
}
