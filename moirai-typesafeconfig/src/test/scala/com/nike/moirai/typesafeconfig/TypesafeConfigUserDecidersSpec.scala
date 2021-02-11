package com.nike.moirai.typesafeconfig

import com.nike.moirai.config.ConfigDeciders._
import com.nike.moirai.config.ConfigDecisionInput
import com.nike.moirai.resource.FileResourceLoaders
import com.nike.moirai.typesafeconfig.TypesafeConfigReader.TYPESAFE_CONFIG_READER
import com.nike.moirai.{ConfigFeatureFlagChecker, FeatureCheckInput, Suppliers}
import com.typesafe.config.Config
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers}

import scala.collection.mutable

//noinspection TypeAnnotation
class TypesafeConfigUserDecidersSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks {

  describe("A combined enabled-user and proportion-of-users config decider") {
    val resourceLoader = FileResourceLoaders.forClasspathResource("moirai.conf")

    val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
      Suppliers.supplierAndThen(resourceLoader, TypesafeConfigLoader.FROM_STRING),
      enabledUsers(TYPESAFE_CONFIG_READER).or(proportionOfUsers(TYPESAFE_CONFIG_READER))
    )

    describe("a feature specifying both enabledUserIds and an enabledProportion of 0.0") {
      it("should be enabled for enabled users") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("7")) shouldBe true
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("8")) shouldBe true
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("9")) shouldBe true
      }

      it("should be enabled for enabled users from common-value reference") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("42")) shouldBe true
      }

      it("it should not be enabled for non-whitelist users because proportion is 0.0") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("6")) shouldBe false
      }
    }

    describe("a feature specifying only enabledProportion of 1.0") {
      it("should be enabled for all users because proportion is 1.0") {
        featureFlagChecker.isFeatureEnabled("bar", FeatureCheckInput.forUser("7")) shouldBe true
        featureFlagChecker.isFeatureEnabled("bar", FeatureCheckInput.forUser("6")) shouldBe true
        featureFlagChecker.isFeatureEnabled("bar", FeatureCheckInput.forUser("Zaphod Beeblebrox")) shouldBe true
      }
    }

    describe("a feature specifying only enabledUserIds") {
      it("should be enabled for enabled users") {
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("susan")) shouldBe true
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("bill")) shouldBe true
      }


      it("should be enabled for enabled users from common-value reference") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("42")) shouldBe true
      }

      it("should not be enabled for non-enabled users") {
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("jack")) shouldBe false
      }
    }

    describe("a feature present in the config with no configuration entries") {
      it("should not be enabled for any users") {
        featureFlagChecker.isFeatureEnabled("qux", FeatureCheckInput.forUser("42")) shouldBe false
      }
    }

    describe("a feature not specified in the config") {
      it("should not be enabled for any users") {
        featureFlagChecker.isFeatureEnabled("quux", FeatureCheckInput.forUser("42")) shouldBe false
      }
    }
  }

  describe("A proportionOfUsers config decider with featureGroup") {
    val resourceLoader = FileResourceLoaders.forClasspathResource("moirai-feature-group.conf")

    val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
      Suppliers.supplierAndThen(resourceLoader, TypesafeConfigLoader.FROM_STRING),
      proportionOfUsers(TYPESAFE_CONFIG_READER)
    )

    it("should decide features are enabled for the same users if the features are in the same feature group and have the same proportion enabled") {
      val feature1EnabledUsers = mutable.Buffer.empty[String]
      val feature2EnabledUsers = mutable.Buffer.empty[String]
      val feature3EnabledUsers = mutable.Buffer.empty[String]
      val feature4EnabledUsers = mutable.Buffer.empty[String]
      val feature5EnabledUsers = mutable.Buffer.empty[String]

      implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 1000)

      forAll { (userId: String) =>
        List(
          ("feature1", feature1EnabledUsers),
          ("feature2", feature2EnabledUsers),
          ("feature3", feature3EnabledUsers),
          ("feature4", feature4EnabledUsers),
          ("feature5", feature5EnabledUsers)).foreach {
          case (feature, buffer) if featureFlagChecker.isFeatureEnabled(feature, FeatureCheckInput.forUser(userId)) => buffer += userId
          case _ =>
        }
      }

      feature1EnabledUsers should contain theSameElementsAs feature2EnabledUsers
      feature3EnabledUsers should contain theSameElementsAs feature4EnabledUsers

      feature1EnabledUsers should not (contain theSameElementsAs feature3EnabledUsers)
      feature1EnabledUsers should not (contain theSameElementsAs feature5EnabledUsers)

      feature3EnabledUsers should not (contain theSameElementsAs feature5EnabledUsers)
    }
  }

  describe("A featureEnabled config decider") {
    val resourceLoader = FileResourceLoaders.forClasspathResource("moirai.conf")

    val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
      Suppliers.supplierAndThen(resourceLoader, TypesafeConfigLoader.FROM_STRING),
      featureEnabled(TYPESAFE_CONFIG_READER)
    )

    it("should be enabled if the configuration say so") {
      featureFlagChecker.isFeatureEnabled("coffee") shouldBe true
      featureFlagChecker.isFeatureEnabled("coffee", FeatureCheckInput.forUser("42")) shouldBe true
    }

    it("should be disabled if the configuration say so") {
      featureFlagChecker.isFeatureEnabled("tea") shouldBe false
      featureFlagChecker.isFeatureEnabled("tea", FeatureCheckInput.forUser("42")) shouldBe false
    }

    it("should be disabled if the configuration does not specify anything") {
      featureFlagChecker.isFeatureEnabled("water") shouldBe false
      featureFlagChecker.isFeatureEnabled("water", FeatureCheckInput.forUser("42")) shouldBe false
    }
  }

  describe("A custom dimension enabled valued config decider") {
    val resourceLoader = FileResourceLoaders.forClasspathResource("moirai.conf")

    val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
      Suppliers.supplierAndThen(resourceLoader, TypesafeConfigLoader.FROM_STRING),
      TypesafeConfigCustomDeciders.enabledCustomStringDimension("country", "enabledCountries")
    )

    it("should be enabled for a user in an enabled country") {
      val input = FeatureCheckInput.forUser("bob").withAdditionalDimension("country", "Peru")
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe true
    }

    it("should be disabled for a user not in an enabled country") {
      val input = FeatureCheckInput.forUser("bob").withAdditionalDimension("country", "Belgium")
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe false
    }

    it("should be disabled for a user not in a country") {
      val input = FeatureCheckInput.forUser("bob")
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe false
    }

    it("should be disabled for a user with an invalid value for country") {
      val input = FeatureCheckInput.forUser("bob").withAdditionalDimension("country", 8)
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe false
    }
  }
}
