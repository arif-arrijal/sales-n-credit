package com.rijal.salesandcredit.di

import android.content.Context
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.module.report.ReportViewModel
import com.rijal.salesandcredit.module.auth.AuthViewModel
import com.rijal.salesandcredit.module.cost.CostViewModel
import com.rijal.salesandcredit.module.invoice.InvoiceViewModel
import com.rijal.salesandcredit.module.item.ItemViewModel
import com.rijal.salesandcredit.module.person.PersonViewModel
import com.rijal.salesandcredit.module.savings.SavingsViewModel
import com.rijal.salesandcredit.module.search.SearchViewModel
import com.rijal.salesandcredit.module.transaction.CapitalInjectionViewModel
import com.rijal.salesandcredit.module.transaction.TransactionViewModel
import com.rijal.salesandcredit.module.zakat.ZakatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun getAppDb(context: Context): AppDatabase {
    return AppDatabase.getDatabase(context, null)
}

val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
    viewModel { CapitalInjectionViewModel(get()) }
    viewModel { InvoiceViewModel(get()) }
    viewModel { CostViewModel(get()) }
    viewModel { ReportViewModel(get()) }
    viewModel { TransactionViewModel(get()) }
    viewModel { ItemViewModel(get()) }
    viewModel { PersonViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { ZakatViewModel(get()) }
    viewModel { SavingsViewModel() }
}

val daoModule = module {
    single { getAppDb(get()) }
}

val appModules = listOf(viewModelModule, daoModule)

