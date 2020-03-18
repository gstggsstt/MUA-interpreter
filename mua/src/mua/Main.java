package src.mua;

import java.io.*;
import java.util.*;
import java.math.*;
import java.util.Random;

public class Main {

	private static final Scanner cin = new Scanner(System.in);
	private static Scanner tsin = new Scanner("");

	public Main() {
		// TODO Auto-generated constructor stub
	}

	private static String Next(Scanner sin) {
		if (sin.hasNext())
			return sin.next();
		if (tsin.hasNext())
			return tsin.next();
		while (!tsin.hasNext()) {
			String op = cin.nextLine();
			op = ExpandBrace(op);
			tsin.close();
			tsin = new Scanner(op);
		}
		return tsin.next();
	}

	private static String PrintError(String str) {
		System.err.println(str);
		return "";
	}

	// Delete quotation of a word
	private static String TrimWord(String str) {
		if (str.charAt(0) == '"')
			return str.substring(1);
		return str;
	}

	// Delete ':' and 'thing'
	private static String TrimThing(String str, Scanner sin) {
		if (str.charAt(0) == ':')
			return str.substring(1);
		try {
			return Next(sin);
		} catch (Exception e) {
			return "";
		}
	}

	private static boolean IsNumber(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	private static boolean IsBool(String str) {
		return str.equals("true") || str.equals("false");
	}

	// Begin with "
	private static boolean IsWord(String str) {
		str = str.trim();
		return str.charAt(0) == '\"';
	}

	// Valid string
	private static boolean IsValidName(String str) {
		if (!((str.charAt(1) >= 'a' && str.charAt(1) <= 'z') || (str.charAt(1) >= 'A' && str.charAt(1) <= 'Z')
				|| str.charAt(1) == '_'))
			return false;

		for (int i = 1; i < str.length(); i++) {
			if (!((str.charAt(i) >= 'a' && str.charAt(i) <= 'z') || (str.charAt(i) >= 'A' && str.charAt(i) <= 'Z')
					|| (str.charAt(i) >= '0' && str.charAt(i) <= '9') || str.charAt(i) == '_'))
				return false;
		}
		return true;
	}

	// Has variable
	private static boolean IsVariable(String str, Map<String, Object> M) {
		str = TrimWord(str);
		return M.containsKey(str);
	}

	private static boolean IsNumberOp(String op) {
		return op.equals("add") || op.equals("sub") || op.equals("mul") || op.equals("div") || op.equals("mod")
				|| op.charAt(0) == '(' || op.charAt(0) == '-' || op.charAt(0) == '+' || op.equals("random")
				|| op.equals("int") || op.equals("sqrt");
	}

	private static boolean IsCompareOp(String op) {
		return op.equals("eq") || op.equals("gt") || op.equals("lt");
	}

	private static boolean IsLogicOp(String op) {
		return op.equals("and") || op.equals("or") || op.equals("not");
	}

	private static boolean IsIsOp(String op) {
		return op.equals("isname") || op.equals("isnumber") || op.equals("isword") || op.equals("isbool")
				|| op.equals("isempty") || op.equals("islist");
	}

	private static boolean IsBoolOp(String op) {
		return IsCompareOp(op) || IsLogicOp(op) || IsIsOp(op);
	}

	private static boolean IsThing(String op) {
		if (op.equals("thing") || op.charAt(0) == ':')
			return true;
		return false;
	}

	// Get value recursively. e.g. :::a
	private static Object DeThing(String op, Scanner sin, Map<String, Object> M) {
		if (IsThing((String) op)) {
			op = TrimThing((String) op, sin);
			Object obj = DeThing(op, sin, M);
			if (obj instanceof String) {
				if (!M.containsKey((String) obj)) {
					PrintError("ERROR: No variable named \'" + (String) obj + "\' .");
					return null;
				}
				return obj = M.get((String) obj);
			}
			PrintError("WARNING: Too much 'thing' operator, ignored.");
			return obj;
		}
		return op;
	}

	private static String GetRead() {
		return cin.nextLine();
	}

	private static void ClearExprQueue(Deque<Character> oplist, Deque<BigDecimal> numlist) {
		BigDecimal a = numlist.pop();
		while (!numlist.isEmpty()) {
			char op = oplist.pop();
			BigDecimal b = numlist.pop();
			if (op == '+')
				a = a.add(b);
			if (op == '-')
				a = a.subtract(b);
		}
		numlist.add(a);
	}

	private static BigDecimal GetNumberExpr(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		Deque<Character> oplist = new LinkedList<Character>();
		Deque<BigDecimal> numlist = new LinkedList<BigDecimal>();

		numlist.add(GetNumber(sin, M, M_g));
		String op = Next(sin);

		while (op.charAt(0) != ')') {
			oplist.add(op.charAt(0));
			numlist.addLast(GetNumber(sin, M, M_g));
			char ch = op.charAt(0);
			if (ch == '*' || ch == '/' || ch == '%') {
				oplist.removeLast();
				BigDecimal op2 = numlist.removeLast();
				BigDecimal op1 = numlist.removeLast();
				if (ch == '*')
					numlist.addLast(op1.multiply(op2));
				else if (ch == '/')
					numlist.addLast(op1.divide(op2));
				else if (ch == '%')
					numlist.addLast(new BigDecimal(op1.toBigInteger().mod(op2.toBigInteger())));
			}
			op = Next(sin);
		}

		ClearExprQueue(oplist, numlist);
		return numlist.getLast();
	}

	private static BigDecimal GetNumberOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		if (op.charAt(0) == '(')
			return GetNumberExpr(sin, M, M_g);
		if (op.charAt(0) == '-')
			return GetNumber(sin, M, M_g).negate();
		if (op.charAt(0) == '+')
			return GetNumber(sin, M, M_g);

		BigDecimal a = GetNumber(sin, M, M_g);

		if (op.equals("random")) {
			Random rnd = new Random();
			return new BigDecimal(rnd.nextInt(a.intValue()));
		}
		if (op.equals("int"))
			return a.setScale(0, RoundingMode.FLOOR);
		if (op.equals("sqrt"))
			return new BigDecimal(Math.sqrt(a.doubleValue()));

		BigDecimal b = GetNumber(sin, M, M_g);

		if (op.equals("add"))
			return a.add(b);
		if (op.equals("sub"))
			return a.subtract(b);
		if (op.equals("mul"))
			return a.multiply(b);
		if (op.equals("div"))
			return a.divide(b);
		if (op.equals("mod"))
			return new BigDecimal(a.toBigInteger().mod(b.toBigInteger()));
		PrintError("ERROR: A <number> expr is required.");
		return new BigDecimal(0);
	}

	private static BigDecimal GetNumber(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String op = Next(sin);
		if (op.equals("read"))
			op = GetRead();
		if (IsThing(op)) {
			Object obj = DeThing(op, sin, M);
			if (obj == null)
				return new BigDecimal(0);
			if (obj instanceof BigDecimal)
				return (BigDecimal) obj;
		} else if (IsNumber(op))
			return new BigDecimal(op);
		else if (IsNumberOp(op))
			return GetNumberOp(op, sin, M, M_g);
		else if (IsNumber(TrimWord(op))) {
			PrintError("WARNING: Casting word to number.");
			return new BigDecimal(TrimWord(op));
		} else if (IsFunc(op, M))
			return (BigDecimal) GetFromFunc(op, sin, M, M_g);
		PrintError("ERROR: A <number> expr is required.");
		return new BigDecimal(0);
	}

	private static String GetWord(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String op = Next(sin);
		if (op.equals("read"))
			op = GetRead();
		if (IsWord(op))
			return TrimWord(op);
		if (IsWordOp(op))
			return GetWordOp(op, sin, M, M_g);
		if (IsWordListOp(op)) {
			Object temp = GetWordOp(op, sin, M, M_g);
			if (temp instanceof String)
				return (String) temp;
			PrintError("ERROR: Value is not a word.");
			return "";
		} else if (IsFunc(op, M))
			return (String) GetFromFunc(op, sin, M, M_g);
		PrintError("WARNNING: Missing '\"' befor string, treated as a word.");
		return op;
	}

	@SuppressWarnings("unchecked")
	private static boolean GetIsOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		Object obj = GetValue(sin, M, M_g);

		if (op.equals("isname"))
			return IsVariable((String) obj, M);
		if (op.equals("isword"))
			return obj instanceof String;
		if (op.equals("isnumber"))
			return obj instanceof BigDecimal;
		if (op.equals("isbool"))
			return obj instanceof Boolean;
		if (op.equals("islist"))
			return obj instanceof ArrayList;
		if (op.equals("isempty")) {
			if (obj instanceof String)
				return ((String) obj).length() == 0;
			if (obj instanceof ArrayList)
				return ((ArrayList<Object>) obj).size() == 0;
			PrintError("ERROR: Operator 'isempty' needs a <word> or <list>.");
			return false;
		}

		PrintError("ERROR: Unknown operator " + op + " .");
		return false;
	}

	private static boolean GetLogicOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		boolean op1 = GetBool(sin, M, M_g);

		if (op.equals("not"))
			return !op1;

		boolean op2 = GetBool(sin, M, M_g);

		if (op.equals("and"))
			return op1 && op2;
		if (op.equals("or"))
			return op1 || op2;

		PrintError("ERROR: A <bool> expr is required.");
		return false;
	}

	private static boolean GetCompareOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		Object op1 = GetValue(sin, M, M_g);
		Object op2 = GetValue(sin, M, M_g);

		if (op1 instanceof BigDecimal && op2 instanceof BigDecimal) {
			BigDecimal a = (BigDecimal) op1;
			BigDecimal b = (BigDecimal) op2;
			if (op.equals("eq"))
				return a.compareTo(b) == 0;
			if (op.equals("gt"))
				return a.compareTo(b) > 0;
			if (op.equals("lt"))
				return a.compareTo(b) < 0;
			PrintError("ERROR: A <number> expr is required.");
			return false;
		}
		PrintError("WARNING: Cmp between non-number values, cmping as strings.");
		String a = op1.toString();
		String b = op2.toString();
		if (op.equals("eq"))
			return a.compareTo(b) == 0;
		if (op.equals("gt"))
			return a.compareTo(b) > 0;
		if (op.equals("lt"))
			return a.compareTo(b) < 0;
		PrintError("ERROR: A <number> expr is required.");
		return false;
	}

	private static boolean GetBoolOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		if (IsIsOp(op))
			return GetIsOp(op, sin, M, M_g);
		if (IsLogicOp(op))
			return GetLogicOp(op, sin, M, M_g);
		if (IsCompareOp(op))
			return GetCompareOp(op, sin, M, M_g);
		PrintError("ERROR: A <bool> expr is required.");
		return false;
	}

	private static boolean GetBool(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String op = Next(sin);
		if (IsThing(op)) {
			Object obj = DeThing(op, sin, M);
			if (obj == null)
				return false;
			if (obj instanceof Boolean)
				return (Boolean) obj;
		} else if (IsBool(op))
			return Boolean.parseBoolean(op);
		else if (IsBoolOp(op))
			return GetBoolOp(op, sin, M, M_g);
		else if (IsBool(TrimWord(op))) {
			PrintError("WARNING: Casting word to bool.");
			return Boolean.parseBoolean(TrimWord(op));
		} else if (IsFunc(op, M))
			return (Boolean) GetFromFunc(op, sin, M, M_g);
		PrintError("ERROR: A <bool> expr is required.");
		return false;
	}

	private static boolean IsList(String op) {
		return op.charAt(0) == '[';
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<Object> GetList(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		if (op.equals(""))
			op = Next(sin); // manual call
		if (IsThing(op)) {
			return (ArrayList<Object>) DeThing(op, sin, M);
		}
		if (IsListOp(op))
			return GetListOp(op, sin, M, M_g);
		if (IsWordListOp(op)) {
			Object temp = GetWordOp(op, sin, M, M_g);
			if (temp instanceof ArrayList)
				return (ArrayList<Object>) temp;
			PrintError("ERROR: Value is not a list.");
			return new ArrayList<Object>();
		} else if (IsFunc(op, M))
			return (ArrayList<Object>) GetFromFunc(op, sin, M, M_g);

		if (op.charAt(0) == '[') {
			ArrayList<Object> L = new ArrayList<Object>();
			while (true) {
				op = Next(sin);
				if (op.charAt(0) == '[')
					L.add(GetList(op, sin, M, M_g));
				else if (op.charAt(0) == ']')
					return L;
				else
					L.add(op);
			}
		}
		return new ArrayList<Object>();
	}

	private static boolean IsWordListOp(String op) {
		return op.equals("first") || op.equals("last") || op.equals("butfirst") || op.equals("butlast");
	}

	private static boolean IsWordOp(String op) {
		return op.equals("word");
	}

	private static boolean IsListOp(String op) {
		return op.equals("sentence") || op.equals("list") || op.equals("join");
	}

	private static String GetWordOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		if (op.equals("word")) {
			String s1 = GetWord(sin, M, M_g);
			String s2 = GetValue(sin, M, M_g).toString();
			return s1 + s2;
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	private static ArrayList<Object> GetListOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		if (op.equals("sentence")) {
			Object obj1 = GetValue(sin, M, M_g);
			Object obj2 = GetValue(sin, M, M_g);
			ArrayList<Object> L = new ArrayList<Object>();
			if (!(obj1 instanceof ArrayList))
				L.add(obj1);
			else
				for (Object t : (ArrayList<Object>) obj1)
					L.add(t);
			if (!(obj2 instanceof ArrayList))
				L.add(obj2);
			else
				for (Object t : (ArrayList<Object>) obj2)
					L.add(t);
			return L;
		}
		if (op.equals("list")) {
			Object obj1 = GetValue(sin, M, M_g);
			Object obj2 = GetValue(sin, M, M_g).toString();
			ArrayList<Object> L = new ArrayList<Object>();
			L.add(obj1);
			L.add(obj2);
			return L;
		}
		if (op.equals("join")) {
			ArrayList<Object> L = GetList("", sin, M, M_g);
			Object obj = GetValue(sin, M, M_g);
			L.add(obj);
			return L;
		}
		return new ArrayList<Object>();
	}

	@SuppressWarnings("unchecked")
	private static Object GetWordListOp(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		if (op.equals("first")) {
			Object obj = GetValue(sin, M, M_g);
			if (obj instanceof String)
				return ((String) obj).substring(0, 1);
			if (obj instanceof ArrayList)
				return ((ArrayList<Object>) obj).get(0);
			return obj.toString().substring(0, 1);
		}
		if (op.equals("last")) {
			Object obj = GetValue(sin, M, M_g);
			if (obj instanceof String)
				return ((String) obj).substring(((String) obj).length() - 1);
			if (obj instanceof ArrayList)
				return ((ArrayList<Object>) obj).get(((ArrayList<Object>) obj).size() - 1);
			return obj.toString().substring(((String) obj).length() - 1);
		}
		if (op.equals("butfirst")) {
			Object obj = GetValue(sin, M, M_g);
			if (obj instanceof String)
				return ((String) obj).substring(1);
			if (obj instanceof ArrayList) {
				ArrayList<Object> tempL = (ArrayList<Object>) ((ArrayList<Object>) obj).clone();
				tempL.remove(0);
				return tempL;
			}
			return obj.toString().substring(1);
		}
		if (op.equals("butlast")) {
			Object obj = GetValue(sin, M, M_g);
			if (obj instanceof String)
				return ((String) obj).substring(0, ((String) obj).length() - 1);
			if (obj instanceof ArrayList) {
				ArrayList<Object> tempL = (ArrayList<Object>) ((ArrayList<Object>) obj).clone();
				tempL.remove(tempL.size() - 1);
				return tempL;
			}
			return obj.toString().substring(0, ((String) obj).length() - 1);
		}
		return new ArrayList<Object>();
	}

	private static Object GetValue(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String op = Next(sin);
		if (IsThing(op)) {
			return DeThing(op, sin, M);
		} else if (op.equals("read"))
			op = GetRead();

		if (IsNumberOp(op))
			return GetNumberOp(op, sin, M, M_g);
		else if (IsBoolOp(op))
			return GetBoolOp(op, sin, M, M_g);
		else if (IsWordOp(op))
			return GetWordOp(op, sin, M, M_g);
		else if (IsListOp(op))
			return GetListOp(op, sin, M, M_g);
		else if (IsWordListOp(op))
			return GetWordListOp(op, sin, M, M_g);
		else if (IsNumber(op))
			return new BigDecimal(op);
		else if (IsWord(op))
			return TrimWord(op);
		else if (IsBool(op))
			return Boolean.parseBoolean(op);
		else if (IsList(op))
			return GetList(op, sin, M, M_g);
		else if (IsFunc(op, M))
			return GetFromFunc(op, sin, M, M_g);
		PrintError("ERROR: Need ':' or '\"' before a word.");
		return null;
	}

	@SuppressWarnings({ "unchecked" })
	private static Object GetFromFunc(String op, Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		ExeRunFunc((ArrayList<Object>) M.get(op), sin, M, M_g);
		if (!M_g.containsKey("#return")) {
			PrintError("ERROR: Function has no return value.");
			return null;
		}
		Object temp = M_g.get("#return");
		M_g.remove("#return");
		return temp;
	}

	private static boolean IsFunc(String op, Map<String, Object> M) {
		return (M.containsKey(op) && M.get(op) instanceof ArrayList);
	}

	private static String GetName(Scanner sin, Map<String, Object> M) {
		String op = Next(sin);
		if (IsThing(op)) {
			Object obj = DeThing(op, sin, M);
			return (String) obj;
		}
		if (op.equals("read"))
			op = GetRead();
		if (!IsWord(op))
			return PrintError("ERROR: Need '\"' befor a word.");
		if (!IsValidName(op))
			return PrintError(String.format("ERROR: '%s' is not a name.", op));
		return op.substring(1);
	}

	private static void ExeMake(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String op1 = GetName(sin, M);
		if (op1.equals(""))
			return;
		Object op2 = GetValue(sin, M, M_g);
		if (op2 == null) {
			PrintError("ERROR: Error occoured when getting value.");
			return;
		}
		M.put(op1, op2);
	}

	private static void ExeErase(Scanner sin, Map<String, Object> M) {
		String op = GetName(sin, M);
		if (M.containsKey(op))
			M.remove(op);
		else
			PrintError(String.format("WARNNING: Name '%s' is not found.", op));
	}

	private static void ExePrint(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		try {
			Object temp = GetValue(sin, M, M_g);
			if (!(temp instanceof ArrayList))
				System.out.println(temp.toString());
			else
				System.out.println(temp.toString().substring(1, temp.toString().length() - 1).replace(",", ""));
		} catch (Exception e) {
		}
	}

	private static void ExeReadList(Scanner sin, Map<String, Object> M) {
		String op1 = GetName(sin, M);
		String op2 = GetRead();
		ArrayList<Object> L = new ArrayList<Object>(Arrays.asList(op2.split(" ")));
		if (L.size() == 0)
			PrintError("WARNNING: No element in list.");
		M.put(op1, L);
	}

	private static void SetArgs(Scanner sin, Map<String, Object> OldLocal, Map<String, Object> NewLocal,
			Map<String, Object> Global, ArrayList<Object> Args) {
		for (Object x : Args) {
			if (x instanceof String)
				NewLocal.put((String) x, GetValue(sin, OldLocal, Global));
		}
	}

	private static void RunFunc(Scanner sin, String Code, Map<String, Object> OldLocal, Map<String, Object> Global,
			ArrayList<Object> Args) {
		Map<String, Object> NewLocal = new HashMap<String, Object>();
		NewLocal.putAll(Global);
		SetArgs(sin, OldLocal, NewLocal, Global, Args);
		try {
			Call(Code, NewLocal, Global);
		} catch (Exception e) {
		}
	}

	private static void Call(String Code, Map<String, Object> Local, Map<String, Object> Global) throws Exception {
		Code = ExpandBrace(Code);
		Scanner ssin = new Scanner(Code);
		while (ssin.hasNext()) {
			ExeCmd(ssin, Local, Global);
		}
		ssin.close();
	}

	@SuppressWarnings("unchecked")
	private static String ListToCode(ArrayList<Object> L) {
		String Code = "";
		for (Object x : L) {
			if (x instanceof ArrayList)
				Code += "[" + ListToCode((ArrayList<Object>) x) + "] ";
			else
				Code += (String) x + " ";
		}
		return Code;
	}

	@SuppressWarnings("unchecked")
	private static void ExeRunFunc(ArrayList<Object> L, Scanner sin, Map<String, Object> Local,
			Map<String, Object> Global) {
		String Code = ListToCode((ArrayList<Object>) L.get(1));
		RunFunc(sin, Code, Local, Global, (ArrayList<Object>) L.get(0));
	}

	private static void ExeRepeat(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) throws Exception {
		int n = Integer.parseInt(GetNumber(sin, M, M_g).toString());
		ArrayList<Object> L = GetList("", sin, M, M_g);
		String Code = ListToCode(L);
		for (int i = 0; i < n; ++i)
			Call(Code, M, M);
	}

	private static void ExeRun(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		Map<String, Object> NewLocal = new HashMap<String, Object>();
		NewLocal.putAll(M_g);
		ArrayList<Object> L = GetList("", sin, M, M_g);
		String Code = ListToCode(L);
		try {
			Call(Code, NewLocal, M_g);
		} catch (Exception e) {
		}
	}

	private static void ExeOutput(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		M_g.put("#return", GetValue(sin, M, M_g));
	}

	private static void ExeIf(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) throws Exception {
		boolean cond = GetBool(sin, M, M_g);
		ArrayList<Object> L1 = GetList("", sin, M, M_g);
		ArrayList<Object> L2 = GetList("", sin, M, M_g);
		if (cond)
			Call(ListToCode(L1), M, M_g);
		else
			Call(ListToCode(L2), M, M_g);
	}

	private static void ExeExport(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String name = GetName(sin, M);
		if (M.containsKey(name))
			M_g.put(name, M.get(name));
		else
			PrintError("WARNING: Name not exist.");
	}

	private static void ExeWait(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		BigDecimal x = GetNumber(sin, M, M_g);
		try {
			Thread.sleep(x.intValue());
		} catch (Exception e) {
		}
	}

	private static void ExeSave(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String fileContent = "";
		for (String x : M.keySet())
			fileContent += "make \"" + x + " " + M.get(x).toString() + "\n";
		fileContent = fileContent.replace(',', ' ');

		String filename = GetWord(sin, M, M_g);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(fileContent);
			writer.close();
			PrintError("INFO: Saved successfully.");
		} catch (Exception e) {
			PrintError("ERROR: Failed saving to file.");
		}
	}

	private static void ExeLoad(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) {
		String filename = GetWord(sin, M, M_g);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String ln;
			while ((ln = reader.readLine()) != null)
				Call(ln, M, M_g);
			PrintError("INFO: Loaded successfully.");
			reader.close();
		} catch (Exception e) {
			PrintError("ERROR: Failed loading file.");
		}
	}

	@SuppressWarnings("unchecked")
	private static void ExeCmd(Scanner sin, Map<String, Object> M, Map<String, Object> M_g) throws Exception {
		String op = Next(sin);
		try {
			if (op.equals("make"))
				ExeMake(sin, M, M_g);
			else if (op.equals("erase"))
				ExeErase(sin, M);
			else if (op.equals("print"))
				ExePrint(sin, M, M_g);
			else if (op.equals("readlist"))
				ExeReadList(sin, M);
			else if (op.equals("repeat"))
				ExeRepeat(sin, M, M_g);
			else if (op.equals("run"))
				ExeRun(sin, M, M_g);
			else if (op.equals("output"))
				ExeOutput(sin, M, M_g);
			else if (op.equals("if"))
				ExeIf(sin, M, M_g);
			else if (op.equals("export"))
				ExeExport(sin, M, M_g);
			else if (op.equals("wait"))
				ExeWait(sin, M, M_g);
			else if (op.equals("erall"))
				M.clear();
			else if (op.equals("poall"))
				System.out.println(M.keySet().toString());
			else if (M.containsKey(op) && M.get(op) instanceof ArrayList)
				ExeRunFunc((ArrayList<Object>) M.get(op), sin, M, M_g);
			else if (op.equals("stop"))
				throw new Exception();
			else if (op.equals("save"))
				ExeSave(sin, M, M_g);
			else if (op.equals("load"))
				ExeLoad(sin, M, M_g);
			else
				PrintError(String.format("WARNNING: Unknown or useless operator '%s'" + ", skiped.", op));
		} catch (NoSuchElementException e) {
			PrintError("ERROR: Arguments not enough.");
		}

		return;
	}

	private static void LoadConst(Map<String, Object> M) {
		M.put("pi", new BigDecimal("3.14159"));
	}

	private static String ExpandBrace(String str) {
		int cnt = 0;
		String res = "";
		for (int i = 0; i < str.length(); ++i) {
			char ch = str.charAt(i);
			if (ch == '[') { cnt++; res += " [ "; }
			else if (ch == ']') { cnt--; res += " ] "; }
			else if (cnt == 0) {
				if (ch == '(') res += " ( ";
				else if (ch == ')') res += " ) ";
				else if (ch == '+') res += " + ";
				else if (ch == '-') res += " - ";
				else if (ch == '*') res += " * ";
				else if (ch == '/') res += " / ";
				else if (ch == '%') res += " % ";
				else res += ch;
			} else res += ch;
		}
		return res;
	}

	public static void main(String[] args) throws Exception {
		String op;
		Map<String, Object> M = new HashMap<String, Object>();
		LoadConst(M);
		while (cin.hasNext()) {
			op = cin.nextLine();
			while (tsin.hasNext())
				op = tsin.nextLine() + " " + op;
			if (op.equals(""))
				continue;
			op = ExpandBrace(op);
			Scanner sin = new Scanner(op);
			ExeCmd(sin, M, M);
			sin.close();
		}
		cin.close();
		tsin.close();
	}

}
