<SCRIPT LANGUAGE="JavaScript">
<!-- Original: wsabstract.com -->

<!-- This script and many more are available free online at -->
<!-- The JavaScript Source!! http://javascript.internet.com -->

<!-- Begin
function checkrequired(which)
{
  var pass=true;
  var prefix_len;
  if (document.images)
  {
    for (i=0;i<which.length;i++)
    {
      var tempobj=which.elements[i];
      if (tempobj.name.substring(0,4)=="req_")
      {
        if (((tempobj.type=="text"||tempobj.type=="textarea") &&
            tempobj.value=='')||(tempobj.type.toString().charAt(0)=="s" &&
            tempobj.selectedIndex==0))
        {
          pass=false;
          prefix_len=4;
          break;
        }
      }
      else if (tempobj.name.substring(0,7)=="reqnum_")
      {
        if (((tempobj.type=="text"||tempobj.type=="textarea") &&
            tempobj.value=='')||(tempobj.type.toString().charAt(0)=="s" &&
            tempobj.selectedIndex==0))
        {
          pass=false;
          prefix_len=7;
          break;
        }
      }
    }
  }
  if (!pass)
  {
    shortFieldName=tempobj.name.substring(prefix_len,30).toUpperCase();
    alert("Please make sure the "+shortFieldName+" field was properly completed.");
    return false;
  }
  else
    return true;
} // End, checkrequired()



function valid_num(field)
{
  var valid = "0123456789"
  var ok = "yes";
  var temp;
  for (var i=0; i<field.value.length; i++)
  {
    temp = "" + field.value.substring(i, i+1);
    if (valid.indexOf(temp) == "-1") ok = "no";
  }

  if (ok == "no")
  {
    alert("Invalid entry - must be numeric.");
    field.focus();
    field.select();
  }
} // End, valid_num()

function valid_min(field)
{
  var valid = "0123456789"
  var ok = "yes";
  var temp;
  for (var i=0; i<field.value.length; i++)
  {
    temp = "" + field.value.substring(i, i+1);
    if (valid.indexOf(temp) == "-1") ok = "no";
  }

  if (ok == "no")
  {
    alert("Invalid entry - must be numeric.");
    field.focus();
    field.select();
  }
  else
  {
    if (field.value < 0 || field.value > 15)
    {
      alert("Invalid entry - minutes must be 0-15.");
      field.focus();
      field.select();
    }
 }
} // End, valid_min()

function valid_sec(field)
{
  var valid = "0123456789"
  var ok = "yes";
  var temp;
  for (var i=0; i<field.value.length; i++)
  {
    temp = "" + field.value.substring(i, i+1);
    if (valid.indexOf(temp) == "-1") ok = "no";
  }

  if (ok == "no")
  {
    alert("Invalid entry - must be numeric.");
    field.focus();
    field.select();
  }
  else
  {
    if (field.value < 0 || field.value > 60)
    {
      alert("Invalid entry - seconds must be 0-60.");
      field.focus();
      field.select();
    }
 }
} // End, valid_sec()
//  End -->
</script>
