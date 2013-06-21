curity
======

A Java library with data structures developed for various projects. Many of the structures have to do with concurrency.

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

https://github.com/ClickerMonkey/curity/tree/master/build

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

https://github.com/ClickerMonkey/curity/tree/master/Testing/org/magnos/util
