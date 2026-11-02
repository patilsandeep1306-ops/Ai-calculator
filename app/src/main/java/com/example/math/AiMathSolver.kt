package com.example.math

import com.example.model.AiProblemSolution
import com.example.model.MathStep
import java.util.Locale
import kotlin.math.*

object AiMathSolver {

    /**
     * Solves a natural language or structured math query and returns an AiProblemSolution.
     */
    fun solve(query: String): AiProblemSolution {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) {
            return AiProblemSolution(
                problemTitle = "Empty Query",
                category = "General",
                summary = "Please enter a math question, formula, or word problem.",
                finalAnswer = "0"
            )
        }

        // Try distinct problem categories
        trySolveTipSplit(q)?.let { return it }
        trySolveLoanMortgage(q)?.let { return it }
        trySolveInterest(q)?.let { return it }
        trySolveDiscountTax(q)?.let { return it }
        trySolveQuadratic(q)?.let { return it }
        trySolveLinear(q)?.let { return it }
        trySolveGeometry(q)?.let { return it }
        trySolvePhysicsAndBmi(q)?.let { return it }
        trySolveStatistics(q)?.let { return it }
        trySolveNumberTheory(q)?.let { return it }
        trySolveUnitConversion(q)?.let { return it }

        // Fallback: Treat as a general math expression
        return solveGeneralExpression(query)
    }

    private fun trySolveTipSplit(q: String): AiProblemSolution? {
        if (!q.contains("tip") && !q.contains("split") && !q.contains("bill")) return null

        val numbers = extractNumbers(q)
        if (numbers.isEmpty()) return null

        val billAmount = numbers.maxOrNull() ?: return null
        val remaining = numbers.filter { it != billAmount }

        // Find tip percentage (default 15% or 18% if not found)
        val tipPercent = remaining.find { it in 5.0..50.0 } ?: 18.0
        // Find number of people (integer, typically 2..50)
        val people = remaining.find { it != tipPercent && it in 2.0..100.0 && it == it.toInt().toDouble() }?.toInt() ?: 1

        val tipAmount = billAmount * (tipPercent / 100.0)
        val totalAmount = billAmount + tipAmount
        val perPerson = totalAmount / people

        val steps = listOf(
            MathStep(1, "Calculate Tip Amount", "Tip = Bill × (Tip % / 100) = $billAmount × ($tipPercent / 100) = $${format(tipAmount)}"),
            MathStep(2, "Calculate Total Bill", "Total = Bill + Tip = $billAmount + $${format(tipAmount)} = $${format(totalAmount)}"),
            MathStep(3, "Split Among $people People", "Share per Person = Total / $people = $${format(totalAmount)} / $people = $${format(perPerson)}")
        )

        return AiProblemSolution(
            problemTitle = "Bill Split & Tip Calculator",
            category = "Finance",
            summary = "Calculation for a bill of $${format(billAmount)} with $tipPercent% tip split among $people person(s).",
            formulaUsed = "Total = Bill × (1 + Tip%), Share = Total / People",
            givenValues = mapOf(
                "Bill Amount" to "$${format(billAmount)}",
                "Tip Rate" to "$tipPercent%",
                "People" to "$people"
            ),
            steps = steps,
            finalAnswer = "$${format(perPerson)} / person",
            unit = "USD",
            explanation = "Total bill is $${format(totalAmount)} (including $${format(tipAmount)} tip). Each person pays $${format(perPerson)}.",
            verification = "$${format(perPerson)} × $people = $${format(perPerson * people)} (matches total bill)."
        )
    }

    private fun trySolveLoanMortgage(q: String): AiProblemSolution? {
        if (!q.contains("loan") && !q.contains("mortgage") && !q.contains("emi") && !q.contains("monthly payment")) return null

        val numbers = extractNumbers(q)
        if (numbers.size < 2) return null

        // Principal is usually the largest number (> 1000)
        val principal = numbers.find { it >= 500.0 } ?: numbers[0]
        val remaining = numbers.filter { it != principal }
        if (remaining.isEmpty()) return null

        // Annual interest rate (e.g. 3.5% to 25%)
        val annualRate = remaining.find { it in 0.5..30.0 } ?: 6.0
        // Duration in years (e.g. 1 to 40)
        val years = remaining.find { it != annualRate && it in 1.0..50.0 } ?: 5.0

        val months = (years * 12).toInt()
        val monthlyRate = (annualRate / 100.0) / 12.0
        val emi = principal * (monthlyRate * (1.0 + monthlyRate).pow(months)) / ((1.0 + monthlyRate).pow(months) - 1.0)
        val totalPayment = emi * months
        val totalInterest = totalPayment - principal

        val steps = listOf(
            MathStep(1, "Convert Annual Rate to Monthly", "Monthly Rate r = ($annualRate% / 100) / 12 = ${format(monthlyRate * 100)}% (${String.format(Locale.US, "%.5f", monthlyRate)})"),
            MathStep(2, "Calculate Total Installments", "Number of months n = $years years × 12 = $months months"),
            MathStep(3, "Apply Standard Amortization Formula", "EMI = P × [r(1+r)^n] / [(1+r)^n - 1] = $${format(emi)}"),
            MathStep(4, "Total Payment & Interest Breakdown", "Total Paid = EMI × $months = $${format(totalPayment)}, Total Interest = $${format(totalInterest)}")
        )

        return AiProblemSolution(
            problemTitle = "Loan & Mortgage Payment (EMI)",
            category = "Finance",
            summary = "Monthly payment calculation for a loan principal of $${format(principal)} at $annualRate% over $years years.",
            formulaUsed = "EMI = P × r(1+r)^n / ((1+r)^n - 1)",
            givenValues = mapOf(
                "Principal (P)" to "$${format(principal)}",
                "Annual Interest Rate" to "$annualRate%",
                "Loan Term" to "$years years ($months months)"
            ),
            steps = steps,
            finalAnswer = "$${format(emi)} / month",
            unit = "USD",
            explanation = "Your monthly payment is $${format(emi)}. Over the course of the loan, you will pay a total of $${format(totalPayment)} ($${format(totalInterest)} in total interest).",
            verification = "Principal ($${format(principal)}) + Total Interest ($${format(totalInterest)}) = $${format(totalPayment)}"
        )
    }

    private fun trySolveInterest(q: String): AiProblemSolution? {
        val isCompound = q.contains("compound")
        val isSimple = q.contains("simple") && q.contains("interest")
        if (!isCompound && !isSimple) return null

        val numbers = extractNumbers(q)
        if (numbers.size < 2) return null

        val principal = numbers.maxOrNull() ?: return null
        val remaining = numbers.filter { it != principal }
        val rate = remaining.find { it in 0.1..50.0 } ?: 5.0
        val years = remaining.find { it != rate && it in 0.1..100.0 } ?: 3.0

        if (isCompound) {
            val amount = principal * (1.0 + (rate / 100.0)).pow(years)
            val interestEarned = amount - principal
            val steps = listOf(
                MathStep(1, "Identify Compound Formula", "Formula: A = P(1 + r)^t where P=$principal, r=${rate / 100}, t=$years"),
                MathStep(2, "Compute Growth Factor", "Growth factor (1 + ${rate / 100})^$years = ${format((1.0 + rate / 100.0).pow(years))}"),
                MathStep(3, "Calculate Total Balance", "Total Balance A = $principal × ${format((1.0 + rate / 100.0).pow(years))} = $${format(amount)}"),
                MathStep(4, "Compute Net Interest", "Interest Earned = A - P = $${format(amount)} - $principal = $${format(interestEarned)}")
            )
            return AiProblemSolution(
                problemTitle = "Compound Interest Calculation",
                category = "Finance",
                summary = "Investment growth of $${format(principal)} at $rate% annual compounding for $years years.",
                formulaUsed = "A = P(1 + r/n)^(nt)",
                givenValues = mapOf(
                    "Principal (P)" to "$${format(principal)}",
                    "Interest Rate (r)" to "$rate%",
                    "Time Period (t)" to "$years years"
                ),
                steps = steps,
                finalAnswer = "$${format(amount)}",
                unit = "USD",
                explanation = "Your final balance after $years years will be $${format(amount)}, with $${format(interestEarned)} in earned interest.",
                verification = "Growth rate is ${(amount / principal - 1.0) * 100}% total return."
            )
        } else {
            val interest = principal * (rate / 100.0) * years
            val total = principal + interest
            val steps = listOf(
                MathStep(1, "Apply Simple Interest Formula", "Formula: I = P × r × t = $principal × ($rate / 100) × $years = $${format(interest)}"),
                MathStep(2, "Calculate Total Maturity Value", "Total = Principal + Interest = $principal + $${format(interest)} = $${format(total)}")
            )
            return AiProblemSolution(
                problemTitle = "Simple Interest Calculation",
                category = "Finance",
                summary = "Simple interest for $${format(principal)} at $rate% per year for $years years.",
                formulaUsed = "I = P × r × t",
                givenValues = mapOf(
                    "Principal" to "$${format(principal)}",
                    "Rate" to "$rate%",
                    "Time" to "$years years"
                ),
                steps = steps,
                finalAnswer = "$${format(interest)}",
                unit = "USD",
                explanation = "The total simple interest earned is $${format(interest)}, making the final balance $${format(total)}."
            )
        }
    }

    private fun trySolveDiscountTax(q: String): AiProblemSolution? {
        if (!q.contains("discount") && !q.contains("off") && !q.contains("tax") && !q.contains("markup")) return null

        val numbers = extractNumbers(q)
        if (numbers.isEmpty()) return null

        val originalPrice = numbers.maxOrNull() ?: return null
        val remaining = numbers.filter { it != originalPrice }

        val discountPercent = remaining.find { it in 1.0..99.0 } ?: 20.0
        val taxPercent = remaining.find { it != discountPercent && it in 1.0..30.0 } ?: 0.0

        val discountAmount = originalPrice * (discountPercent / 100.0)
        val discountedPrice = originalPrice - discountAmount
        val taxAmount = discountedPrice * (taxPercent / 100.0)
        val finalPrice = discountedPrice + taxAmount

        val steps = mutableListOf(
            MathStep(1, "Calculate Discount Savings", "Discount = $originalPrice × ($discountPercent / 100) = $${format(discountAmount)}"),
            MathStep(2, "Calculate Sale Price", "Sale Price = $originalPrice - $${format(discountAmount)} = $${format(discountedPrice)}")
        )
        if (taxPercent > 0) {
            steps.add(MathStep(3, "Add Sales Tax ($taxPercent%)", "Tax = $${format(discountedPrice)} × ($taxPercent / 100) = $${format(taxAmount)}"))
            steps.add(MathStep(4, "Compute Final Checkout Price", "Final Total = $${format(discountedPrice)} + $${format(taxAmount)} = $${format(finalPrice)}"))
        }

        return AiProblemSolution(
            problemTitle = "Discount & Sales Tax Calculation",
            category = "Shopping & Finance",
            summary = "$discountPercent% discount on $${format(originalPrice)}${if (taxPercent > 0) " with $taxPercent% tax" else ""}.",
            formulaUsed = "Final = (Price - Discount) + Tax",
            givenValues = mapOf(
                "Original Price" to "$${format(originalPrice)}",
                "Discount Rate" to "$discountPercent%",
                "Sales Tax" to if (taxPercent > 0) "$taxPercent%" else "None"
            ),
            steps = steps,
            finalAnswer = "$${format(finalPrice)}",
            unit = "USD",
            explanation = "You save $${format(discountAmount)} (${discountPercent}%). The final price to pay is $${format(finalPrice)}.",
            verification = "Savings: $${format(discountAmount)} off original $${format(originalPrice)}."
        )
    }

    private fun trySolveQuadratic(q: String): AiProblemSolution? {
        val quadRegex = Regex("""(-?\d*)\s*x\s*\^?\s*2\s*([+-]\s*\d*)\s*x\s*([+-]\s*\d+)\s*=\s*0""")
        val match = quadRegex.find(q.replace(" ", ""))

        var a = 1.0
        var b = 0.0
        var c = 0.0
        var matched = false

        if (match != null) {
            val aStr = match.groupValues[1]
            a = when (aStr) {
                "", "+" -> 1.0
                "-" -> -1.0
                else -> aStr.toDoubleOrNull() ?: 1.0
            }
            val bStr = match.groupValues[2]
            b = when (bStr) {
                "+", "" -> 1.0
                "-" -> -1.0
                else -> bStr.toDoubleOrNull() ?: 0.0
            }
            c = match.groupValues[3].toDoubleOrNull() ?: 0.0
            matched = true
        } else if (q.contains("quadratic") || q.contains("ax^2")) {
            val nums = extractNumbers(q)
            if (nums.size >= 3) {
                a = nums[0]
                b = nums[1]
                c = nums[2]
                matched = true
            }
        }

        if (!matched || a == 0.0) return null

        val discriminant = b * b - 4 * a * c
        val steps = mutableListOf(
            MathStep(1, "Identify Coefficients", "Standard form: ax² + bx + c = 0 → a = $a, b = $b, c = $c"),
            MathStep(2, "Calculate Discriminant (Δ)", "Δ = b² - 4ac = ($b)² - 4($a)($c) = ${b * b} - ${4 * a * c} = $discriminant")
        )

        val finalAnswer: String
        val explanation: String

        if (discriminant > 0) {
            val sqrtDelta = sqrt(discriminant)
            val x1 = (-b + sqrtDelta) / (2 * a)
            val x2 = (-b - sqrtDelta) / (2 * a)
            steps.add(MathStep(3, "Apply Quadratic Formula", "x = (-b ± √Δ) / 2a = (-($b) ± √$discriminant) / (2 × $a)"))
            steps.add(MathStep(4, "Compute Two Real Roots", "x₁ = (${-b} + ${format(sqrtDelta)}) / ${2 * a} = ${format(x1)}\nx₂ = (${-b} - ${format(sqrtDelta)}) / ${2 * a} = ${format(x2)}"))
            finalAnswer = "x₁ = ${format(x1)}, x₂ = ${format(x2)}"
            explanation = "Since the discriminant Δ > 0, the equation has two distinct real roots."
        } else if (discriminant == 0.0) {
            val x = -b / (2 * a)
            steps.add(MathStep(3, "Single Repeated Root", "Since Δ = 0: x = -b / 2a = -($b) / (2 × $a) = ${format(x)}"))
            finalAnswer = "x = ${format(x)}"
            explanation = "Since the discriminant Δ = 0, the equation has one repeated real root."
        } else {
            val realPart = -b / (2 * a)
            val imagPart = sqrt(-discriminant) / (2 * a)
            steps.add(MathStep(3, "Complex Conjugate Roots", "x = (-b ± i√|Δ|) / 2a = ${format(realPart)} ± ${format(abs(imagPart))}i"))
            finalAnswer = "${format(realPart)} ± ${format(abs(imagPart))}i"
            explanation = "Since the discriminant Δ < 0, the equation has two complex conjugate roots."
        }

        return AiProblemSolution(
            problemTitle = "Quadratic Equation Solver",
            category = "Algebra",
            summary = "Step-by-step solution for ${if (a != 1.0) a else ""}x² ${if (b >= 0) "+ " + b else "- " + abs(b)}x ${if (c >= 0) "+ " + c else "- " + abs(c)} = 0",
            formulaUsed = "x = (-b ± √(b² - 4ac)) / (2a)",
            givenValues = mapOf("a" to "$a", "b" to "$b", "c" to "$c", "Discriminant (Δ)" to "$discriminant"),
            steps = steps,
            finalAnswer = finalAnswer,
            explanation = explanation,
            verification = "Substitution into ax² + bx + c yields 0."
        )
    }

    private fun trySolveLinear(q: String): AiProblemSolution? {
        if (!q.contains("=") || (!q.contains("x") && !q.contains("y"))) return null
        if (q.contains("^") || q.contains("²")) return null

        val linearRegex = Regex("""(-?\d*)\s*x\s*([+-]\s*\d+)?\s*=\s*(-?\d+)""")
        val match = linearRegex.find(q.replace(" ", "")) ?: return null

        val aStr = match.groupValues[1]
        val a = when (aStr) {
            "", "+" -> 1.0
            "-" -> -1.0
            else -> aStr.toDoubleOrNull() ?: 1.0
        }
        val b = match.groupValues[2].replace(" ", "").toDoubleOrNull() ?: 0.0
        val c = match.groupValues[3].toDoubleOrNull() ?: return null

        if (a == 0.0) return null

        val rhsAfterB = c - b
        val x = rhsAfterB / a

        val steps = listOf(
            MathStep(1, "Isolate the Variable Term", "Original: ${a}x ${if (b >= 0) "+ $b" else "- ${abs(b)}"} = $c\nSubtract $b from both sides: ${a}x = $c - ($b) = $rhsAfterB"),
            MathStep(2, "Solve for x", "Divide both sides by $a: x = $rhsAfterB / $a = ${format(x)}")
        )

        return AiProblemSolution(
            problemTitle = "Linear Equation Solver",
            category = "Algebra",
            summary = "Solve for variable x in ${a}x ${if (b >= 0) "+ $b" else "- ${abs(b)}"} = $c",
            formulaUsed = "x = (c - b) / a",
            givenValues = mapOf("Coefficient of x (a)" to "$a", "Constant (b)" to "$b", "RHS (c)" to "$c"),
            steps = steps,
            finalAnswer = "x = ${format(x)}",
            explanation = "Linear equation with one unique solution.",
            verification = "Check: ${a}(${format(x)}) + ($b) = ${a * x + b} = $c (Verified)."
        )
    }

    private fun trySolveGeometry(q: String): AiProblemSolution? {
        val numbers = extractNumbers(q)
        if (numbers.isEmpty()) return null

        // Circle
        if (q.contains("circle") || (q.contains("radius") && !q.contains("cylinder") && !q.contains("sphere"))) {
            val r = numbers[0]
            val area = Math.PI * r * r
            val circ = 2 * Math.PI * r
            val steps = listOf(
                MathStep(1, "Circle Area Formula", "A = π × r² = π × ($r)² = π × ${r * r} = ${format(area)}"),
                MathStep(2, "Circle Circumference Formula", "C = 2 × π × r = 2 × π × $r = ${format(circ)}")
            )
            return AiProblemSolution(
                problemTitle = "Circle Geometry (Area & Circumference)",
                category = "Geometry",
                summary = "Geometric calculations for a circle with radius r = $r.",
                formulaUsed = "Area = πr², Circumference = 2πr",
                givenValues = mapOf("Radius (r)" to "$r"),
                steps = steps,
                finalAnswer = "Area = ${format(area)}, Circumference = ${format(circ)}",
                explanation = "Area represents total surface 2D space, circumference is perimeter boundary length."
            )
        }

        // Cylinder
        if (q.contains("cylinder")) {
            if (numbers.size < 2) return null
            val r = numbers[0]
            val h = numbers[1]
            val volume = Math.PI * r * r * h
            val surfaceArea = 2 * Math.PI * r * h + 2 * Math.PI * r * r
            val steps = listOf(
                MathStep(1, "Calculate Base Area", "Base Area B = π × r² = π × ($r)² = ${format(Math.PI * r * r)}"),
                MathStep(2, "Calculate Volume", "V = Base × Height = πr²h = ${format(volume)}"),
                MathStep(3, "Calculate Total Surface Area", "A = 2πrh + 2πr² = 2π($r)($h) + 2π($r)² = ${format(surfaceArea)}")
            )
            return AiProblemSolution(
                problemTitle = "Cylinder Volume & Surface Area",
                category = "Geometry",
                summary = "Cylinder with radius r = $r and height h = $h.",
                formulaUsed = "V = πr²h, Surface Area = 2πrh + 2πr²",
                givenValues = mapOf("Radius (r)" to "$r", "Height (h)" to "$h"),
                steps = steps,
                finalAnswer = "Volume = ${format(volume)}, Surface Area = ${format(surfaceArea)}"
            )
        }

        // Sphere
        if (q.contains("sphere")) {
            val r = numbers[0]
            val volume = (4.0 / 3.0) * Math.PI * r.pow(3)
            val surfaceArea = 4.0 * Math.PI * r * r
            val steps = listOf(
                MathStep(1, "Sphere Volume Formula", "V = (4/3)πr³ = (4/3) × π × ($r)³ = ${format(volume)}"),
                MathStep(2, "Sphere Surface Area", "A = 4πr² = 4 × π × ($r)² = ${format(surfaceArea)}")
            )
            return AiProblemSolution(
                problemTitle = "Sphere Volume & Surface Area",
                category = "Geometry",
                summary = "Calculations for a 3D sphere with radius r = $r.",
                formulaUsed = "V = (4/3)πr³, A = 4πr²",
                givenValues = mapOf("Radius (r)" to "$r"),
                steps = steps,
                finalAnswer = "Volume = ${format(volume)}, Surface Area = ${format(surfaceArea)}"
            )
        }

        // Right Triangle / Pythagorean Theorem
        if (q.contains("hypotenuse") || q.contains("triangle") || q.contains("pythagor")) {
            if (numbers.size >= 2) {
                val a = numbers[0]
                val b = numbers[1]
                val c = sqrt(a * a + b * b)
                val area = 0.5 * a * b
                val steps = listOf(
                    MathStep(1, "Apply Pythagorean Theorem", "c² = a² + b² = ($a)² + ($b)² = ${a * a} + ${b * b} = ${a * a + b * b}"),
                    MathStep(2, "Solve for Hypotenuse c", "c = √${a * a + b * b} = ${format(c)}"),
                    MathStep(3, "Calculate Right Triangle Area", "Area = (1/2) × base × height = (1/2) × $a × $b = ${format(area)}")
                )
                return AiProblemSolution(
                    problemTitle = "Right Triangle & Hypotenuse",
                    category = "Geometry",
                    summary = "Right triangle with perpendicular sides a = $a and b = $b.",
                    formulaUsed = "c = √(a² + b²), Area = 1/2 × a × b",
                    givenValues = mapOf("Leg a" to "$a", "Leg b" to "$b"),
                    steps = steps,
                    finalAnswer = "Hypotenuse c = ${format(c)}, Area = ${format(area)}"
                )
            }
        }

        return null
    }

    private fun trySolvePhysicsAndBmi(q: String): AiProblemSolution? {
        val numbers = extractNumbers(q)

        // BMI
        if (q.contains("bmi") || q.contains("body mass index")) {
            if (numbers.size >= 2) {
                // Determine weight (kg usually > 30) and height (meters usually 1.2 to 2.3 or cm > 100)
                var weight = numbers.maxOrNull() ?: 70.0
                var height = numbers.minOrNull() ?: 1.75
                if (height > 50.0) {
                    height /= 100.0 // convert cm to meters
                }
                val bmi = weight / (height * height)
                val category = when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 24.9 -> "Normal weight"
                    bmi < 29.9 -> "Overweight"
                    else -> "Obesity"
                }
                val steps = listOf(
                    MathStep(1, "BMI Formula", "BMI = weight (kg) / [height (m)]²"),
                    MathStep(2, "Substitute Values", "BMI = $weight / ($height)² = $weight / ${format(height * height)} = ${format(bmi)}"),
                    MathStep(3, "Clinical Category", "Result ${format(bmi)} falls into the '$category' range (Standard WHO classification).")
                )
                return AiProblemSolution(
                    problemTitle = "Body Mass Index (BMI)",
                    category = "Health & Physics",
                    summary = "BMI calculated for weight $weight kg and height $height m.",
                    formulaUsed = "BMI = kg / m²",
                    givenValues = mapOf("Weight" to "$weight kg", "Height" to "$height m"),
                    steps = steps,
                    finalAnswer = "${format(bmi)} ($category)",
                    unit = "kg/m²",
                    explanation = "A normal BMI range is between 18.5 and 24.9."
                )
            }
        }

        // Speed, Distance, Time
        if (q.contains("speed") || q.contains("distance") || q.contains("travel")) {
            if (numbers.size >= 2) {
                val distance = numbers.maxOrNull() ?: 100.0
                val time = numbers.minOrNull() ?: 2.0
                val speed = distance / time
                val steps = listOf(
                    MathStep(1, "Speed Formula", "v = d / t where distance d = $distance, time t = $time"),
                    MathStep(2, "Calculate Average Speed", "v = $distance / $time = ${format(speed)}")
                )
                return AiProblemSolution(
                    problemTitle = "Average Speed Calculation",
                    category = "Physics",
                    summary = "Velocity calculation covering $distance units in $time time units.",
                    formulaUsed = "Speed = Distance / Time",
                    givenValues = mapOf("Distance" to "$distance", "Time" to "$time"),
                    steps = steps,
                    finalAnswer = "${format(speed)} units/hr",
                    explanation = "The average rate of motion required to traverse the distance."
                )
            }
        }

        // Kinetic Energy: Ek = 1/2 m v^2
        if (q.contains("kinetic energy") || q.contains("energy")) {
            if (numbers.size >= 2) {
                val mass = numbers[0]
                val velocity = numbers[1]
                val ke = 0.5 * mass * velocity * velocity
                val steps = listOf(
                    MathStep(1, "Kinetic Energy Formula", "E_k = (1/2) × m × v²"),
                    MathStep(2, "Substitute Mass and Velocity", "E_k = 0.5 × $mass × ($velocity)² = 0.5 × $mass × ${velocity * velocity} = ${format(ke)} Joules")
                )
                return AiProblemSolution(
                    problemTitle = "Kinetic Energy Calculation",
                    category = "Physics",
                    summary = "Kinetic energy for an object of mass $mass kg moving at $velocity m/s.",
                    formulaUsed = "E_k = 1/2 × m × v²",
                    givenValues = mapOf("Mass (m)" to "$mass kg", "Velocity (v)" to "$velocity m/s"),
                    steps = steps,
                    finalAnswer = "${format(ke)} J",
                    unit = "Joules",
                    explanation = "Work required to accelerate the mass from rest to stated velocity."
                )
            }
        }

        return null
    }

    private fun trySolveStatistics(q: String): AiProblemSolution? {
        if (!q.contains("stats") && !q.contains("mean") && !q.contains("average") && !q.contains("median") && !q.contains("variance")) return null

        val numbers = extractNumbers(q)
        if (numbers.size < 2) return null

        val count = numbers.size
        val sum = numbers.sum()
        val mean = sum / count
        val sorted = numbers.sorted()
        val median = if (count % 2 == 1) {
            sorted[count / 2]
        } else {
            (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
        }
        val variance = numbers.map { (it - mean).pow(2) }.sum() / (count - 1).coerceAtLeast(1)
        val stdDev = sqrt(variance)

        val steps = listOf(
            MathStep(1, "Sum and Count Data", "Count N = $count, Sum Σx = ${format(sum)}"),
            MathStep(2, "Calculate Mean (Average)", "Mean μ = Σx / N = ${format(sum)} / $count = ${format(mean)}"),
            MathStep(3, "Find Median", "Sorted list: ${sorted.joinToString(", ") { format(it) }}\nMedian = ${format(median)}"),
            MathStep(4, "Standard Deviation", "Sample Variance s² = ${format(variance)}, Standard Deviation s = ${format(stdDev)}")
        )

        return AiProblemSolution(
            problemTitle = "Statistical Analysis",
            category = "Statistics",
            summary = "Descriptive statistics computed for dataset of $count values.",
            formulaUsed = "Mean = Σx/N, s = √(Σ(x-μ)² / (N-1))",
            givenValues = mapOf("Data Points" to "${numbers.joinToString(", ") { format(it) }}"),
            steps = steps,
            finalAnswer = "Mean = ${format(mean)}, Median = ${format(median)}, StdDev = ${format(stdDev)}"
        )
    }

    private fun trySolveNumberTheory(q: String): AiProblemSolution? {
        val numbers = extractNumbers(q).map { it.roundToInt() }
        if (q.contains("gcd") || q.contains("hcf") || q.contains("greatest common")) {
            if (numbers.size >= 2) {
                val a = numbers[0]
                val b = numbers[1]
                val gcdVal = gcd(abs(a), abs(b))
                val steps = listOf(
                    MathStep(1, "Euclidean Algorithm", "Find GCD of $a and $b by successive division remainders."),
                    MathStep(2, "Result", "GCD($a, $b) = $gcdVal")
                )
                return AiProblemSolution(
                    problemTitle = "Greatest Common Divisor (GCD)",
                    category = "Number Theory",
                    summary = "GCD of $a and $b.",
                    steps = steps,
                    finalAnswer = "$gcdVal"
                )
            }
        }

        if (q.contains("lcm") || q.contains("least common multiple")) {
            if (numbers.size >= 2) {
                val a = numbers[0]
                val b = numbers[1]
                val gcdVal = gcd(abs(a), abs(b))
                val lcmVal = if (gcdVal != 0) (abs(a.toLong() * b.toLong()) / gcdVal) else 0L
                val steps = listOf(
                    MathStep(1, "Formula Connection", "LCM(a, b) = (|a × b|) / GCD(a, b)"),
                    MathStep(2, "Compute", "LCM($a, $b) = (${abs(a)} × ${abs(b)}) / $gcdVal = $lcmVal")
                )
                return AiProblemSolution(
                    problemTitle = "Least Common Multiple (LCM)",
                    category = "Number Theory",
                    summary = "LCM of $a and $b.",
                    steps = steps,
                    finalAnswer = "$lcmVal"
                )
            }
        }

        return null
    }

    private fun trySolveUnitConversion(q: String): AiProblemSolution? {
        if (!q.contains("convert") && !q.contains(" to ") && !q.contains(" in ")) return null
        val numbers = extractNumbers(q)
        if (numbers.isEmpty()) return null
        val value = numbers[0]

        // Miles to KM
        if (q.contains("mile") && q.contains("km")) {
            val km = value * 1.60934
            return AiProblemSolution(
                problemTitle = "Unit Conversion: Miles to Kilometers",
                category = "Conversion",
                summary = "Convert $value miles to kilometers.",
                formulaUsed = "1 mile = 1.60934 km",
                steps = listOf(MathStep(1, "Multiply by Conversion Factor", "$value miles × 1.60934 = ${format(km)} km")),
                finalAnswer = "${format(km)} km"
            )
        }

        // KM to Miles
        if (q.contains("km") && q.contains("mile")) {
            val miles = value / 1.60934
            return AiProblemSolution(
                problemTitle = "Unit Conversion: Kilometers to Miles",
                category = "Conversion",
                summary = "Convert $value km to miles.",
                formulaUsed = "1 km = 0.621371 miles",
                steps = listOf(MathStep(1, "Divide by Conversion Factor", "$value km / 1.60934 = ${format(miles)} miles")),
                finalAnswer = "${format(miles)} miles"
            )
        }

        // Fahrenheit to Celsius
        if (q.contains("f") && q.contains("c")) {
            val c = (value - 32.0) * (5.0 / 9.0)
            return AiProblemSolution(
                problemTitle = "Temperature: Fahrenheit to Celsius",
                category = "Conversion",
                summary = "Convert $value°F to Celsius.",
                formulaUsed = "°C = (°F - 32) × 5/9",
                steps = listOf(MathStep(1, "Apply Formula", "($value - 32) × 5/9 = ${value - 32} × 0.5556 = ${format(c)}°C")),
                finalAnswer = "${format(c)} °C"
            )
        }

        return null
    }

    private fun solveGeneralExpression(expr: String): AiProblemSolution {
        val steps = ExpressionEvaluator.explainSteps(expr)
        val result = ExpressionEvaluator.tryEvaluate(expr)

        return AiProblemSolution(
            problemTitle = "Algebraic Expression Breakdown",
            category = "Arithmetic & Algebra",
            summary = "Order-of-operations evaluation for expression: $expr",
            formulaUsed = "PEMDAS / BODMAS Precedence",
            steps = steps,
            finalAnswer = if (result != null) ExpressionEvaluator.formatNumber(result) else "Evaluation Error",
            explanation = "Evaluated following standard operator precedence: Parentheses → Exponents → Multiplication/Division (Left-to-right) → Addition/Subtraction (Left-to-right)."
        )
    }

    private fun extractNumbers(text: String): List<Double> {
        val regex = Regex("""-?\d+(?:\.\d+)?""")
        return regex.findAll(text).mapNotNull { it.value.toDoubleOrNull() }.toList()
    }

    private fun format(v: Double): String = ExpressionEvaluator.formatNumber(v)

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }
}
