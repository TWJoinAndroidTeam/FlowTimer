package com.example.flowtimerlibrary

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

fun <T> ioFlow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = flow(block).flowOn(Dispatchers.IO)

fun <T, R> zip(
    vararg flows: Flow<T>,
    transform: suspend (List<T>) -> R,
): Flow<R> = when (flows.size) {
    0 -> error("No flows")
    1 -> flows[0].map { transform(listOf(it)) }
    2 -> flows[0].zip(flows[1]) { a, b -> transform(listOf(a, b)) }
    else -> {
        var accFlow: Flow<List<T>> = flows[0].zip(flows[1]) { a, b -> listOf(a, b) }
        for (i in 2 until flows.size) {
            accFlow = accFlow.zip(flows[i]) { list, it ->
                list + it
            }
        }
        accFlow.map(transform)
    }
}

inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
    while (true) {
        val prevValue = value
        val nextValue = function(prevValue)
        if (compareAndSet(prevValue, nextValue)) return
    }
}

suspend inline fun <T> MutableSharedFlow<T>?.fineEmit(obj: T?) = kotlin.run {
    do {
        delay(50)
        val count = this?.subscriptionCount?.value
    } while (count == 0)

    if (obj != null) {
        this?.emit(obj)
    }
}

fun <T> Flow<T>?.launchWhenCreatedFlowLast(lifecycleOwner: LifecycleOwner, collect: suspend (T) -> Unit): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
            this@launchWhenCreatedFlowLast?.collectLatest(collect)
        }
    }
}

fun <T> Flow<T>?.launchWhenCreatedFlow(lifecycleOwner: LifecycleOwner, collect: suspend (T) -> Unit): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
            this@launchWhenCreatedFlow?.collect(collect)
        }
    }
}

fun <T> Flow<T>?.launchWhenStartedFlow(lifecycleOwner: LifecycleOwner, collect: suspend (T) -> Unit): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // {code to collect from viewModel}
            this@launchWhenStartedFlow?.collect(collect)
        }
    }
}

fun <T> Flow<T>?.launchWhenStartedFlowLast(lifecycleOwner: LifecycleOwner, collect: suspend (T) -> Unit): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // {code to collect from viewModel}
            this@launchWhenStartedFlowLast?.collectLatest(collect)
        }
    }
}

fun <T> Flow<T>?.launchWhenResumeFlow(lifecycleOwner: LifecycleOwner, collect: suspend (T) -> Unit): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // {code to collect from viewModel}
            this@launchWhenResumeFlow?.collect(collect)
        }
    }
}

fun <T> Flow<T>?.launchAllLifeFlow(lifecycleOwner: LifecycleOwner, collect: suspend (T) -> Unit): Job {
    return lifecycleOwner.lifecycleScope.launch {
        this@launchAllLifeFlow?.collect(collect)
    }
}

fun <T> Flow<T>?.launchAllLifeFlowLast(lifecycleOwner: LifecycleOwner, collect: suspend (T) -> Unit): Job {
    return lifecycleOwner.lifecycleScope.launch {
        this@launchAllLifeFlowLast?.collectLatest(collect)
    }
}