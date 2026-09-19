package com.luxwallet.app.core.model

import kotlinx.serialization.Serializable

/** Financial apps Lux Wallet is allowed to read notifications from. Nothing else is ever processed. */
@Serializable
enum class SourceApp {
    MYBCA, SEABANK, SHOPEEPAY, GOPAY
}

@Serializable
enum class TransactionDirection { IN, OUT, NONE }

/** Mirrors PRD §10 "Supported transaction types". */
@Serializable
enum class TransactionType {
    EXPENSE,
    INCOME,
    INTERNAL_TRANSFER,
    EXTERNAL_TRANSFER,
    QRIS_PAYMENT,
    EWALLET_TOPUP,
    MARKETPLACE_PAYMENT,
    REFUND,
    FEE,
    INTEREST,
    CASHBACK,
    BALANCE_ADJUSTMENT,
    UNKNOWN
}

@Serializable
enum class ReviewStatus { CONFIRMED, NEEDS_REVIEW, IGNORED }

@Serializable
enum class ParseStatus { PENDING, PARSED, FAILED, IGNORED }

@Serializable
enum class AccountKind { BANK, EWALLET, MANUAL }

@Serializable
enum class AccountProvider { BCA, SEABANK, GOPAY, SHOPEEPAY, CASH, OTHER }

@Serializable
enum class AssetClass {
    BANK, EWALLET, CASH, DEPOSITO, SAHAM, REKSA_DANA, OBLIGASI_SBN,
    EMAS, CRYPTO, PROPERTI, KENDARAAN, PIUTANG, OTHER
}

@Serializable
enum class LiabilityType { CREDIT_CARD, PERSONAL_LOAN, PAYLATER, MORTGAGE, OTHER_DEBT }

@Serializable
enum class RiskProfile { CONSERVATIVE, MODERATE, GROWTH }

@Serializable
enum class BudgetKind { OVERALL, CATEGORY, DISCRETIONARY }

enum class BudgetStatus { ON_TRACK, WATCH, OVER_BUDGET }

@Serializable
enum class RawRetentionPolicy { NEVER, DAYS_7, DAYS_30, INDEFINITE }

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Needs Review reasons, PRD §24. Stored so the UI can explain why a transaction landed here. */
@Serializable
enum class ReviewReason {
    AMOUNT_ONLY_TYPE_UNKNOWN,
    TRANSFER_DESTINATION_UNKNOWN,
    POSSIBLE_DUPLICATE,
    POSSIBLE_INTERNAL_TRANSFER,
    UNKNOWN_MERCHANT,
    CONFLICTING_OBSERVATIONS,
    PARSING_FAILURE,
    LOW_CONFIDENCE,
    ACCOUNT_NOT_CONFIGURED
}
