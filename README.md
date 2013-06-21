curity
======

A Java library with data structures developed for various projects. Many of the structures have to do with concurrency.

**Classes**
- AtomicStack *- a thread-safe wait-free stack implementation.*
- BlockableQueue *- a queue implementation which can toggle between blocking and non-blocking.*
- ConcurrentSet *- a thread-safe set of objects.*
- Files *- file copying functionality.*
- Gate *- a lock that stops all waiting threads until one of the threads open the gate. An item is held behind the gate and all blocking threads receive that object upon opening.*
- Ref *- an interface that merely holds some value (get/set methods).*
- LockRef *- Ref implementation that is an efficient reentrant read-write lock. Readers don't block each other, but a write will.*
- NonNullRef *- Ref implementation that blocks until a non-null value is set*
- Notifier *- A dynamic proxy class used for notifying a list of listeners. Objects of the same type are added, when the proxy().METHOD is called, it calls METHOD on all objects added to the notifier.*
- Release *- A utility for unblocking blocking calls.*
- Signal *- Provides a way for several threads to send signals between each other.*
- Sleepable *-Any entity which blocks its thread for some period of time or until some event occurs and can be nicely awoken.*
- State *- A thread-safe state machine used for keeping track of the state of some object, waiting for a state to occur, and changing states.*

**Example**

```java
/** Snippet with the utilities that ARE NOT related to concurrency. See JavaDocs to view 
    snippets of concurrent classes. **/

// Copy one file/dir to another file/dir.
Files.copy(new File("src_dir"), new File("dst_dir"));

// A listener to the bar event
public interface Foo {
    public void onBar();
}

// Create a notifier to invoke all Foos with one invocation.
Notifier<Foo> notifier = Notifier.create(Foo.class);
notifier.add(new Foo());
notifier.add(new Foo());
 
// Invoke onBar to all Foo added to notifier
notifier.proxy().onBar();
```

**Builds**
- [curity-1.0.0.jar](https://github.com/ClickerMonkey/curity/blob/master/build/curity-1.0.0.jar?raw=true)
- [curity-src-1.0.0.jar](https://github.com/ClickerMonkey/curity/blob/master/build/curity-src-1.0.0.jar?raw=true) *- includes source code*

**Projects using curity:**
- [taskaroo](https://github.com/ClickerMonkey/taskaroo)
- [statastic](https://github.com/ClickerMonkey/statastic)
- [daperz](https://github.com/ClickerMonkey/daperz)
- [surfice](https://github.com/ClickerMonkey/surfice)
- [zource](https://github.com/ClickerMonkey/zource)
- [falcon](https://github.com/ClickerMonkey/falcon)
- [buffero](https://github.com/ClickerMonkey/buffero)

**Dependencies**
- [testility](https://github.com/ClickerMonkey/testility) *for unit tests*

**Testing Examples**
- [Testing/org/magnos/util](https://github.com/ClickerMonkey/curity/tree/master/Testing/org/magnos/util)
