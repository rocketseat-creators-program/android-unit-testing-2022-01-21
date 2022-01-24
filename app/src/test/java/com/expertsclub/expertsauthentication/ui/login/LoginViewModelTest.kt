package com.expertsclub.expertsauthentication.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.expertsclub.expertsauthentication.data.manager.LocalPersistenceManager
import com.expertsclub.expertsauthentication.data.repository.UserRepository
import com.expertsclub.expertsauthentication.data.repository.UserRepositoryImpl
import com.expertsclub.expertsauthentication.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.lang.IllegalStateException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    @Mock
    private lateinit var repository: UserRepository

    @Mock
    private lateinit var loginStateDataObserver: Observer<LoginViewModel.LoginState>

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @Before
    fun initViewModel() {
        val loginUseCase = LoginUseCase(repository, Dispatchers.Main)
        viewModel = LoginViewModel(loginUseCase)
        viewModel.loginStateData.observeForever(loginStateDataObserver)
    }

    @Test
    fun `should return login success state when login is successfully`() {
        // Arrange
        val email = "teste@user.com"
        val password = "1234"

        // Act
        viewModel.login(email, password)

        // Assert
        verify(loginStateDataObserver).onChanged(LoginViewModel.LoginState.LoginSuccess)
    }

    @Test
    fun `should return show error state when login is invalid due to empty email`() {
        // Arrange
        val email = ""
        val password = "1234"

        // Act
        viewModel.login(email, password)

        // Assert
        verify(loginStateDataObserver).onChanged(LoginViewModel.LoginState.ShowError)
    }

    @Test
    fun `should return show error state when login is invalid due to empty password`() {
        // Arrange
        val email = "teste@user.com"
        val password = ""

        // Act
        viewModel.login(email, password)

        // Assert
        verify(loginStateDataObserver).onChanged(LoginViewModel.LoginState.ShowError)
    }

    @Test
    fun `should return show error state when login is invalid due to empties email and password`() = runBlockingTest {
        // Arrange
        val email = "teste@user.com"
        val password = "1234"

        whenever(repository.saveUserId(any())).thenThrow(IllegalStateException::class.java)

        // Act
        viewModel.login(email, password)

        // Assert
        verify(loginStateDataObserver).onChanged(LoginViewModel.LoginState.ShowError)
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }
}