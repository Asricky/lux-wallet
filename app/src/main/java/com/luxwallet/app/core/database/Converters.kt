package com.luxwallet.app.core.database

import androidx.room.TypeConverter
import com.luxwallet.app.core.model.AccountKind
import com.luxwallet.app.core.model.AccountProvider
import com.luxwallet.app.core.model.AssetClass
import com.luxwallet.app.core.model.BudgetKind
import com.luxwallet.app.core.model.LiabilityType
import com.luxwallet.app.core.model.ParseStatus
import com.luxwallet.app.core.model.ReviewReason
import com.luxwallet.app.core.model.ReviewStatus
import com.luxwallet.app.core.model.RiskProfile
import com.luxwallet.app.core.model.SourceApp
import com.luxwallet.app.core.model.TransactionDirection
import com.luxwallet.app.core.model.TransactionType

/** Room type converters for enums; stored as their [Enum.name] so column values stay human-readable in DB inspectors. */
class Converters {
    @TypeConverter fun sourceAppToString(v: SourceApp?): String? = v?.name
    @TypeConverter fun stringToSourceApp(v: String?): SourceApp? = v?.let { SourceApp.valueOf(it) }

    @TypeConverter fun directionToString(v: TransactionDirection?): String? = v?.name
    @TypeConverter fun stringToDirection(v: String?): TransactionDirection? = v?.let { TransactionDirection.valueOf(it) }

    @TypeConverter fun typeToString(v: TransactionType?): String? = v?.name
    @TypeConverter fun stringToType(v: String?): TransactionType? = v?.let { TransactionType.valueOf(it) }

    @TypeConverter fun reviewStatusToString(v: ReviewStatus?): String? = v?.name
    @TypeConverter fun stringToReviewStatus(v: String?): ReviewStatus? = v?.let { ReviewStatus.valueOf(it) }

    @TypeConverter fun reviewReasonToString(v: ReviewReason?): String? = v?.name
    @TypeConverter fun stringToReviewReason(v: String?): ReviewReason? = v?.let { ReviewReason.valueOf(it) }

    @TypeConverter fun parseStatusToString(v: ParseStatus?): String? = v?.name
    @TypeConverter fun stringToParseStatus(v: String?): ParseStatus? = v?.let { ParseStatus.valueOf(it) }

    @TypeConverter fun accountKindToString(v: AccountKind?): String? = v?.name
    @TypeConverter fun stringToAccountKind(v: String?): AccountKind? = v?.let { AccountKind.valueOf(it) }

    @TypeConverter fun accountProviderToString(v: AccountProvider?): String? = v?.name
    @TypeConverter fun stringToAccountProvider(v: String?): AccountProvider? = v?.let { AccountProvider.valueOf(it) }

    @TypeConverter fun assetClassToString(v: AssetClass?): String? = v?.name
    @TypeConverter fun stringToAssetClass(v: String?): AssetClass? = v?.let { AssetClass.valueOf(it) }

    @TypeConverter fun liabilityTypeToString(v: LiabilityType?): String? = v?.name
    @TypeConverter fun stringToLiabilityType(v: String?): LiabilityType? = v?.let { LiabilityType.valueOf(it) }

    @TypeConverter fun budgetKindToString(v: BudgetKind?): String? = v?.name
    @TypeConverter fun stringToBudgetKind(v: String?): BudgetKind? = v?.let { BudgetKind.valueOf(it) }

    @TypeConverter fun riskProfileToString(v: RiskProfile?): String? = v?.name
    @TypeConverter fun stringToRiskProfile(v: String?): RiskProfile? = v?.let { RiskProfile.valueOf(it) }
}
