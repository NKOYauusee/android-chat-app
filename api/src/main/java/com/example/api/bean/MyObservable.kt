package com.example.api.bean

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class MyObservable<T> : Observer<T> {
    override fun onSubscribe(d: Disposable) {
    }

    override fun onError(e: Throwable) {
        failed(e)
    }

    override fun onComplete() {
    }

    override fun onNext(t: T & Any) {
        success(t)

    }

    abstract fun success(res: T)


    abstract fun failed(e: Throwable)
}