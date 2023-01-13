@file:Name("GainAnalysisKt")

package com.github.secretx33.kotlinplayground.mp3

import jdk.jfr.Name
import java.util.TreeMap

private val ABYule = treeMapOf(
    96000 to arrayOf(0.006471345933032, -7.22103125152679, -0.02567678242161, 24.7034187975904, 0.049805860704367, -52.6825833623896, -0.05823001743528, 77.4825736677539, 0.040611847441914, -82.0074753444205, -0.010912036887501, 63.1566097101925, -0.00901635868667, -34.889569769245, 0.012448886238123, 13.2126852760198, -0.007206683749426, -3.09445623301669, 0.002167156433951, 0.340344741393305, -0.000261819276949),
    88200 to arrayOf(0.015415414474287, -7.19001570087017, -0.07691359399407, 24.4109412087159, 0.196677418516518, -51.6306373580801, -0.338855114128061, 75.3978476863163, 0.430094579594561, -79.4164552507386, -0.415015413747894, 61.0373661948115, 0.304942508151101, -33.7446462547014, -0.166191795926663, 12.8168791146274, 0.063198189938739, -3.01332198541437, -0.015003978694525, 0.223619893831468, 0.001748085184539),
    64000 to arrayOf(0.021776466467053, -5.74819833657784, -0.062376961003801, 16.246507961894, 0.107731165328514, -29.9691822642542, -0.150994515142316, 40.027597579378, 0.170334807313632, -40.3209196052655, -0.157984942890531, 30.8542077487718, 0.121639833268721, -17.5965138737281, -0.074094040816409, 7.10690214103873, 0.031282852041061, -1.82175564515191, -0.00755421235941, 0.223619893831468, 0.00117925454213),
    48000 to arrayOf(0.03857599435200, -3.84664617118067, -0.02160367184185, 7.81501653005538, -0.00123395316851, -11.34170355132042, -0.00009291677959, 13.05504219327545, -0.01655260341619, -12.28759895145294, 0.02161526843274, 9.48293806319790, -0.02074045215285, -5.87257861775999, 0.00594298065125, 2.75465861874613, 0.00306428023191, -0.86984376593551, 0.00012025322027, 0.13919314567432, 0.00288463683916),
    44100 to arrayOf(0.05418656406430, -3.47845948550071, -0.02911007808948, 6.36317777566148, -0.00848709379851, -8.54751527471874, -0.00851165645469, 9.47693607801280, -0.00834990904936, -8.81498681370155, 0.02245293253339, 6.85401540936998, -0.02596338512915, -4.39470996079559, 0.01624864962975, 2.19611684890774, -0.00240879051584, -0.75104302451432, 0.00674613682247, 0.13149317958808, -0.00187763777362),
    32000 to arrayOf(0.15457299681924, -2.37898834973084, -0.09331049056315, 2.84868151156327, -0.06247880153653, -2.64577170229825, 0.02163541888798, 2.23697657451713, -0.05588393329856, -1.67148153367602, 0.04781476674921, 1.00595954808547, 0.00222312597743, -0.45953458054983, 0.03174092540049, 0.16378164858596, -0.01390589421898, -0.05032077717131, 0.00651420667831, 0.02347897407020, -0.00881362733839),
    24000 to arrayOf(0.30296907319327, -1.61273165137247, -0.22613988682123, 1.07977492259970, -0.08587323730772, -0.25656257754070, 0.03282930172664, -0.16276719120440, -0.00915702933434, -0.22638893773906, -0.02364141202522, 0.39120800788284, -0.00584456039913, -0.22138138954925, 0.06276101321749, 0.04500235387352, -0.00000828086748, 0.02005851806501, 0.00205861885564, 0.00302439095741, -0.02950134983287),
    22050 to arrayOf(0.33642304856132, -1.49858979367799, -0.25572241425570, 0.87350271418188, -0.11828570177555, 0.12205022308084, 0.11921148675203, -0.80774944671438, -0.07834489609479, 0.47854794562326, -0.00469977914380, -0.12453458140019, -0.00589500224440, -0.04067510197014, 0.05724228140351, 0.08333755284107, 0.00832043980773, -0.04237348025746, -0.01635381384540, 0.02977207319925, -0.01760176568150),
    16000 to arrayOf(0.44915256608450, -0.62820619233671, -0.14351757464547, 0.29661783706366, -0.22784394429749, -0.37256372942400, -0.01419140100551, 0.00213767857124, 0.04078262797139, -0.42029820170918, -0.12398163381748, 0.22199650564824, 0.04097565135648, 0.00613424350682, 0.10478503600251, 0.06747620744683, -0.01863887810927, 0.05784820375801, -0.03193428438915, 0.03222754072173, 0.00541907748707),
    12000 to arrayOf(0.56619470757641, -1.04800335126349, -0.75464456939302, 0.29156311971249, 0.16242137742230, -0.26806001042947, 0.16744243493672, 0.00819999645858, -0.18901604199609, 0.45054734505008, 0.30931782841830, -0.33032403314006, -0.27562961986224, 0.06739368333110, 0.00647310677246, -0.04784254229033, 0.08647503780351, 0.01639907836189, -0.03788984554840, 0.01807364323573, -0.00588215443421),
    11025 to arrayOf(0.58100494960553, -0.51035327095184, -0.53174909058578, -0.31863563325245, -0.14289799034253, -0.20256413484477, 0.17520704835522, 0.14728154134330, 0.02377945217615, 0.38952639978999, 0.15558449135573, -0.23313271880868, -0.25344790059353, -0.05246019024463, 0.01628462406333, -0.02505961724053, 0.06920467763959, 0.02442357316099, -0.03721611395801, 0.01818801111503, -0.00749618797172),
    8000 to arrayOf(0.53648789255105, -0.25049871956020, -0.42163034350696, -0.43193942311114, -0.00275953611929, -0.03424681017675, 0.04267842219415, -0.04678328784242, -0.10214864179676, 0.26408300200955, 0.14590772289388, 0.15113130533216, -0.02459864859345, -0.17556493366449, -0.11202315195388, -0.18823009262115, -0.04060034127000, 0.05477720428674, 0.04788665548180, 0.04704409688120, -0.02217936801134),
)

private val ABButter = treeMapOf(
    96000 to arrayOf(0.99308203517541, -1.98611621154089, -1.98616407035082, 0.986211929160751, 0.99308203517541),
    88200 to arrayOf(0.992472550461293,-1.98488843762334, -1.98494510092258, 0.979389350028798, 0.992472550461293),
    64000 to arrayOf(0.989641019334721,-1.97917472731008, -1.97928203866944, 0.979389350028798, 0.989641019334721),
    48000 to arrayOf(0.98621192462708, -1.97223372919527, -1.97242384925416, 0.97261396931306, 0.98621192462708 ),
    44100 to arrayOf(0.98500175787242, -1.96977855582618, -1.97000351574484, 0.97022847566350, 0.98500175787242),
    32000 to arrayOf(0.97938932735214, -1.95835380975398, -1.95877865470428, 0.95920349965459, 0.97938932735214),
    24000 to arrayOf(0.97531843204928, -1.95002759149878, -1.95063686409857, 0.95124613669835, 0.97531843204928),
    22050 to arrayOf(0.97316523498161, -1.94561023566527, -1.94633046996323, 0.94705070426118, 0.97316523498161),
    16000 to arrayOf(0.96454515552826, -1.92783286977036, -1.92909031105652, 0.93034775234268, 0.96454515552826),
    12000 to arrayOf(0.96009142950541, -1.91858953033784, -1.92018285901082, 0.92177618768381, 0.96009142950541),
    11025 to arrayOf(0.95856916599601, -1.91542108074780, -1.91713833199203, 0.91885558323625, 0.95856916599601),
    8000 to arrayOf(0.94597685600279, -1.88903307939452, -1.89195371200558, 0.89487434461664, 0.94597685600279),
)

//private fun applyButterworthFilter(
//    input: Collection<Double>,
//    sampleRate: Int,
//): Collection<Double> {
//    val key = setOf(ABButter.floorKey(sampleRate), ABButter.ceilingKey(sampleRate))
//        .minBy { (sampleRate - it).absoluteValue }
//    val kernel = ABButter[key]!!
//
//    ABYule.
//}

private fun <K : Any, V> treeMapOf(vararg pairs: Pair<K, V>): TreeMap<K, V> =
    TreeMap<K, V>().apply { putAll(pairs) }