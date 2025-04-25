# Dirty Checking Mongo Spring

## Overview
`dirtychecking-mongo-spring` is a library that enables dirty checking when using Spring Data MongoDB.

## Installation
To use this library, add the following dependency to your `pom.xml` (for Maven):

```xml
<dependency>
    <groupId>io.github.dirtychecking-mongo</groupId>
    <artifactId>dirtychecking-mongo-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

For Gradle:

```groovy
dependencies {
    implementation 'io.github.dirtychecking-mongo:dirtychecking-mongo-spring:1.0.0'
}
```

## Usage
### Configuration
Since it supports auto-configuration, no additional setup is required.

### Example
Updates can be made without explicitly calling `save`, as shown below.

##### TestUpdater (Before applying dirty checking)
```java
@Service
@RequiredArgsConstructor
public class UserUpdater {
    private final UserRepository userRepository;
    
    public void update(User updatableUser, User user) {
        updatableUser.update(user);
        userRepository.save(user);
    }
}
```

##### TestUpdater (After applying dirty checking)
```java
@Service
@RequiredArgsConstructor
public class UserUpdater {
    public void update(User updatableUser, User user) {
        updatableUser.update(user);
    }
}
```