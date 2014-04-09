import org.migror.model.rules.{Rule, RuleSet, MigrationRules}

val rules = MigrationRules(List(
  RuleSet(fileFilter = ".*CovService.*",
          imports = "nl.vektis.downloads.vz37_38.v10.wsdl.*,javax.xml.ws.WebServiceRef,nl.agisweb.common.util.SOAPHelper",
          rules = List(
    Rule(find = "nl.vektis.downloads.vz3738.v10.wsdl",
         replace = "nl.vektis.downloads.vz37_38.v10.wsdl"),
    Rule(find = "@EJB()",
         replace = "")
  )),
  RuleSet(fileFilter = ".*CorMessageJdbcHelper.*",
          imports = "nl.agisweb.basisverzekering.cormessage.CorMessage,nl.agisweb.common.util.DateHelper",
          rules = List(
    Rule(find = "(\\n[ \\t]+)pstmt\\.setDate\\((.*), (.*)\\)(.*)",
         replace = "$1pstmt\\.setDate\\($2, DateHelper.toSql\\($3\\)\\)$4",
         regex = true)
  ))
))
