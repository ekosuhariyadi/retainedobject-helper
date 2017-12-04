Android Architecture Component ViewModel is really helpful for me.  
But I have to agree with this presentation [Android Architecture Components Considered Harmful](https://academy.realm.io/posts/android-architecture-components-considered-harmful-mobilization)

There is miss leading naming here, Android Arch's ViewModel is not **VM** on **MVVM**,  
but simply "holder object that survive on config changes".
What if I using **Presenter** on **MVP** or **MVI** or [Elm Architecture](https://github.com/ekosuhariyadi/elm-android)

So, I decided to rewrite Android Arch's ViewModel as RetainedObject

**Disclaimer**  
Source code is based on Android Arch's ViewModel that rewritten in Kotlin