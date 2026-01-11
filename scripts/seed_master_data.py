import json
import os
import sys
from datetime import date
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, urljoin
from urllib.request import Request, urlopen


def _read_json(resp):
    body = resp.read().decode("utf-8")
    if not body:
        return None
    return json.loads(body)


def api_request(method, base_url, path, token=None, payload=None, params=None, company_id=None):
    url = urljoin(base_url.rstrip("/") + "/", path.lstrip("/"))
    if params:
        url = url + "?" + urlencode(params)
    headers = {"Accept": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if company_id:
        headers["X-Company-Id"] = str(company_id)
    data = None
    if payload is not None:
        headers["Content-Type"] = "application/json"
        data = json.dumps(payload).encode("utf-8")
    req = Request(url, method=method, headers=headers, data=data)
    with urlopen(req) as resp:
        return _read_json(resp)


def login(base_url, username, password, company_id=None):
    payload = {"username": username, "password": password}
    resp = api_request("POST", base_url, "/api/auth/login", payload=payload, company_id=company_id)
    return resp["token"]


def get_list(base_url, path, token=None, params=None, company_id=None):
    data = api_request("GET", base_url, path, token=token, params=params, company_id=company_id)
    return data if isinstance(data, list) else []


def find_by_key(items, key, value):
    value_norm = str(value).strip().lower()
    for item in items:
        candidate = str(item.get(key, "")).strip().lower()
        if candidate == value_norm:
            return item
    return None


def create_if_missing(base_url, list_path, create_path, token, key, value, payload, params=None, company_id=None):
    items = get_list(base_url, list_path, token=token, params=params, company_id=company_id)
    existing = find_by_key(items, key, value)
    if existing:
        return existing
    return api_request("POST", base_url, create_path, token=token, payload=payload, company_id=company_id)


def create_party_bank_if_missing(base_url, party_id, token, account_no, payload, company_id=None):
    list_path = f"/api/parties/{party_id}/bank-accounts"
    accounts = get_list(base_url, list_path, token=token, company_id=company_id)
    existing = find_by_key(accounts, "accountNo", account_no)
    if existing:
        return existing
    return api_request("POST", base_url, list_path, token=token, payload=payload, company_id=company_id)


def main():
    base_url = os.getenv("BASE_URL", "http://localhost:8080")
    token = os.getenv("TOKEN")
    username = os.getenv("USERNAME", "admin")
    password = os.getenv("PASSWORD", "admin123")
    company_id = os.getenv("COMPANY_ID", "1")

    if not token:
        if not username or not password:
            print("Set TOKEN or USERNAME/PASSWORD to authenticate.", file=sys.stderr)
            sys.exit(1)
        token = login(base_url, username, password, company_id=company_id)

    results = {}

    # UOMs
    uoms = [
        {"code": "KG", "description": "Kilogram"},
        {"code": "GM", "description": "Gram"},
        {"code": "MG", "description": "Milligram"},
        {"code": "QTL", "description": "Quintal"},
        {"code": "TON", "description": "Tonne"},
        {"code": "BAG", "description": "Bag"},
    ]
    created_uoms = {}
    for uom in uoms:
        resp = create_if_missing(
            base_url,
            "/api/uoms",
            "/api/uoms",
            token,
            "code",
            uom["code"],
            uom,
            params={"q": uom["code"], "limit": 50},
            company_id=company_id,
        )
        created_uoms[uom["code"]] = resp["id"]
    kg_id = created_uoms.get("KG")
    if kg_id:
        uom_updates = [
            {"code": "KG", "description": "Kilogram", "baseUomId": None, "conversionFactor": None},
            {"code": "GM", "description": "Gram", "baseUomId": kg_id, "conversionFactor": 0.001},
            {"code": "MG", "description": "Milligram", "baseUomId": kg_id, "conversionFactor": 0.000001},
            {"code": "QTL", "description": "Quintal", "baseUomId": kg_id, "conversionFactor": 100},
            {"code": "TON", "description": "Tonne", "baseUomId": kg_id, "conversionFactor": 1000},
            {"code": "BAG", "description": "Bag", "baseUomId": None, "conversionFactor": None},
        ]
        for update in uom_updates:
            uom_id = created_uoms.get(update["code"])
            if not uom_id:
                continue
            api_request(
                "PUT",
                base_url,
                f"/api/uoms/{uom_id}",
                token=token,
                payload=update,
                company_id=company_id,
            )
    results["uoms"] = created_uoms

    # Banks
    banks = [
        {"name": "State Bank of India", "branch": "Kochi Main", "accNo": "1234567890", "ifsc": "SBIN0000001", "swiftCode": "SBININBB", "type": "SAVINGS"},
        {"name": "Federal Bank", "branch": "Thrissur", "accNo": "9876543210", "ifsc": "FDRL0000002", "swiftCode": "FDRLINBB", "type": "CURRENT"},
    ]
    created_banks = {}
    for bank in banks:
        resp = create_if_missing(
            base_url,
            "/api/banks",
            "/api/banks",
            token,
            "name",
            bank["name"],
            bank,
            params={"q": bank["name"], "limit": 50},
            company_id=company_id,
        )
        created_banks[bank["name"]] = resp["id"]
    results["banks"] = created_banks

    # Locations
    locations = [
        {"name": "Kerala Main Godown", "code": "LOC-KL-GD-MAIN", "locationType": "GODOWN"},
        {"name": "Kerala Intake Bin", "code": "BIN-KL-INTAKE", "locationType": "BIN"},
    ]
    created_locations = {}
    for loc in locations:
        resp = create_if_missing(
            base_url,
            "/api/locations",
            "/api/locations",
            token,
            "code",
            loc["code"],
            loc,
            company_id=company_id,
        )
        created_locations[loc["code"]] = resp["id"]
    results["locations"] = created_locations

    # Godowns
    godowns = [
        {"name": "Kochi Godown", "location": "Kochi, Kerala"},
        {"name": "Palakkad Godown", "location": "Palakkad, Kerala"},
        {"name": "Thrissur Godown", "location": "Thrissur, Kerala"},
    ]
    created_godowns = {}
    for gd in godowns:
        resp = create_if_missing(
            base_url,
            "/api/godowns",
            "/api/godowns",
            token,
            "name",
            gd["name"],
            gd,
            params={"q": gd["name"], "limit": 50},
            company_id=company_id,
        )
        created_godowns[gd["name"]] = resp["id"]
    results["godowns"] = created_godowns

    # Brokers
    brokers = [
        {"name": "Kerala Grain Brokers", "code": "BRK-KER-01", "brokerCommissionType": "PERCENT", "brokerCommissionRate": 1.0, "brokeragePaidBy": "COMPANY"},
        {"name": "South Coast Brokers", "code": "BRK-KER-02", "brokerCommissionType": "PERCENT", "brokerCommissionRate": 0.75, "brokeragePaidBy": "COMPANY"},
    ]
    created_brokers = {}
    broker_parties = {}
    for broker in brokers:
        resp = create_if_missing(
            base_url,
            "/api/brokers",
            "/api/brokers",
            token,
            "code",
            broker["code"],
            broker,
            params={"q": broker["code"], "limit": 50},
            company_id=company_id,
        )
        created_brokers[broker["code"]] = resp["id"]
        broker_parties[broker["code"]] = resp.get("partyId")
    results["brokers"] = created_brokers

    # Vehicles
    vehicles = [
        {"vehicleNo": "KL07AB1234", "vehicleType": "TRUCK", "registrationDate": str(date.today())},
        {"vehicleNo": "KL11CD5678", "vehicleType": "TRUCK", "registrationDate": str(date.today())},
        {"vehicleNo": "KL13EF9012", "vehicleType": "TRUCK", "registrationDate": str(date.today())},
    ]
    created_vehicles = {}
    for vehicle in vehicles:
        resp = create_if_missing(
            base_url,
            "/api/vehicles",
            "/api/vehicles",
            token,
            "vehicleNo",
            vehicle["vehicleNo"],
            vehicle,
            params={"q": vehicle["vehicleNo"], "limit": 50},
            company_id=company_id,
        )
        created_vehicles[vehicle["vehicleNo"]] = resp["id"]
    results["vehicles"] = created_vehicles

    # Suppliers
    supplier_bank_id = next(iter(created_banks.values()), None)
    suppliers = [
        {"name": "Kerala Paddy Suppliers", "code": "SUP-PADDY-KL", "bankId": supplier_bank_id, "creditPeriod": 30},
        {"name": "Wheat Traders Kerala", "code": "SUP-WHEAT-KL", "bankId": supplier_bank_id, "creditPeriod": 21},
        {"name": "Maida Mills Co", "code": "SUP-MAIDA-KL", "bankId": supplier_bank_id, "creditPeriod": 15},
        {"name": "Packing Material Kerala", "code": "SUP-PACK-KL", "bankId": supplier_bank_id, "creditPeriod": 30},
    ]
    created_suppliers = {}
    supplier_parties = {}
    for supplier in suppliers:
        resp = create_if_missing(
            base_url,
            "/api/suppliers",
            "/api/suppliers",
            token,
            "code",
            supplier["code"],
            supplier,
            params={"q": supplier["code"], "limit": 50},
            company_id=company_id,
        )
        created_suppliers[supplier["code"]] = resp["id"]
        supplier_parties[supplier["code"]] = resp.get("partyId")
    results["suppliers"] = created_suppliers

    # Customers
    customer_bank_id = supplier_bank_id
    customers = [
        {"name": "Mothers Retail Kerala", "code": "CUST-MOTH-KL", "bankId": customer_bank_id, "creditPeriod": 15},
        {"name": "Kerala Wholesale Foods", "code": "CUST-KL-WHOLE", "bankId": customer_bank_id, "creditPeriod": 30},
        {"name": "South India Distributors", "code": "CUST-SI-DIST", "bankId": customer_bank_id, "creditPeriod": 20},
    ]
    created_customers = {}
    customer_parties = {}
    for customer in customers:
        resp = create_if_missing(
            base_url,
            "/api/customers",
            "/api/customers",
            token,
            "code",
            customer["code"],
            customer,
            params={"q": customer["code"], "limit": 50},
            company_id=company_id,
        )
        created_customers[customer["code"]] = resp["id"]
        customer_parties[customer["code"]] = resp.get("partyId")
    results["customers"] = created_customers

    # Items
    default_uom_id = created_uoms.get("KG")
    bag_uom_id = created_uoms.get("BAG")
    items = [
        {"name": "Raw Paddy", "sku": "RM-PADDY", "uomId": default_uom_id},
        {"name": "Wheat Grain", "sku": "RM-WHEAT", "uomId": default_uom_id},
        {"name": "Maida", "sku": "FG-MAIDA", "uomId": default_uom_id},
        {"name": "Rice", "sku": "FG-RICE", "uomId": default_uom_id},
        {"name": "Wheat Flour", "sku": "FG-WHEAT-FLOUR", "uomId": default_uom_id},
        {"name": "Rice Bran", "sku": "BP-RICE-BRAN", "uomId": default_uom_id},
        {"name": "Broken Rice", "sku": "BP-BROKEN-RICE", "uomId": default_uom_id},
        {"name": "Packing Bags", "sku": "RM-PACK-BAGS", "uomId": bag_uom_id or default_uom_id},
    ]
    created_items = {}
    for item in items:
        resp = create_if_missing(
            base_url,
            "/api/items",
            "/api/items",
            token,
            "sku",
            item["sku"],
            item,
            params={"q": item["sku"], "limit": 50},
            company_id=company_id,
        )
        created_items[item["sku"]] = resp["id"]
    results["items"] = created_items

    # TDS Rules
    tds_rules = [
        {
            "sectionCode": "194C",
            "ratePercent": 1.0,
            "thresholdAmount": 30000,
            "effectiveFrom": str(date.today()),
            "effectiveTo": None,
        }
    ]
    created_tds = {}
    for rule in tds_rules:
        resp = create_if_missing(
            base_url,
            "/api/tds-rules",
            "/api/tds-rules",
            token,
            "sectionCode",
            rule["sectionCode"],
            rule,
            company_id=company_id,
        )
        created_tds[rule["sectionCode"]] = resp["id"]
    results["tds_rules"] = created_tds

    # Deduction/Charge Types
    charge_types = [
        {"code": "UNLOAD", "name": "Unloading Charges", "defaultCalcType": "PERCENT", "defaultRate": 1.0, "isDeduction": False, "enabled": True},
        {"code": "COOLIE", "name": "Coolie Charges", "defaultCalcType": "FLAT", "defaultRate": 300, "isDeduction": False, "enabled": True},
        {"code": "CUT", "name": "Cutting Charges", "defaultCalcType": "FLAT", "defaultRate": 250, "isDeduction": False, "enabled": True},
        {"code": "TRAN", "name": "Transport Charges", "defaultCalcType": "FLAT", "defaultRate": 800, "isDeduction": False, "enabled": True},
        {"code": "MOIST", "name": "Moisture Deduction", "defaultCalcType": "PERCENT", "defaultRate": 1.0, "isDeduction": True, "enabled": True},
        {"code": "BAGWT", "name": "Bag Weight Deduction", "defaultCalcType": "FLAT", "defaultRate": 100, "isDeduction": True, "enabled": True},
    ]
    created_charge_types = {}
    for charge in charge_types:
        resp = create_if_missing(
            base_url,
            "/api/deduction-charge-types",
            "/api/deduction-charge-types",
            token,
            "code",
            charge["code"],
            charge,
            company_id=company_id,
        )
        created_charge_types[charge["code"]] = resp["id"]
    results["deduction_charge_types"] = created_charge_types

    # Expense Parties
    expense_parties = [
        {"name": "Fuel Expense", "partyType": "EXPENSE"},
        {"name": "Unloading Labor", "partyType": "EXPENSE"},
        {"name": "Packaging Expense", "partyType": "EXPENSE"},
        {"name": "Transport Services", "partyType": "EXPENSE"},
        {"name": "Cutting Services", "partyType": "EXPENSE"},
    ]
    created_expense_parties = {}
    for party in expense_parties:
        resp = create_if_missing(
            base_url,
            "/api/expense-parties",
            "/api/expense-parties",
            token,
            "name",
            party["name"],
            party,
            company_id=company_id,
        )
        created_expense_parties[party["name"]] = resp["id"]
    results["expense_parties"] = created_expense_parties

    # Party bank accounts
    party_bank_results = {}
    for code, party_id in supplier_parties.items():
        if not party_id:
            continue
        payload = {
            "bankName": "State Bank of India",
            "branch": "Kochi Main",
            "accountNo": f"{code}-001",
            "ifsc": "SBIN0000001",
            "swiftCode": "SBININBB",
            "accountType": "CURRENT",
            "isDefault": True,
            "active": True,
        }
        resp = create_party_bank_if_missing(base_url, party_id, token, payload["accountNo"], payload, company_id=company_id)
        party_bank_results[f"supplier-{code}"] = resp["id"]
    for code, party_id in customer_parties.items():
        if not party_id:
            continue
        payload = {
            "bankName": "Federal Bank",
            "branch": "Thrissur",
            "accountNo": f"{code}-001",
            "ifsc": "FDRL0000002",
            "swiftCode": "FDRLINBB",
            "accountType": "SAVINGS",
            "isDefault": True,
            "active": True,
        }
        resp = create_party_bank_if_missing(base_url, party_id, token, payload["accountNo"], payload, company_id=company_id)
        party_bank_results[f"customer-{code}"] = resp["id"]
    for code, party_id in broker_parties.items():
        if not party_id:
            continue
        payload = {
            "bankName": "State Bank of India",
            "branch": "Kochi Main",
            "accountNo": f"{code}-001",
            "ifsc": "SBIN0000001",
            "swiftCode": "SBININBB",
            "accountType": "CURRENT",
            "isDefault": True,
            "active": True,
        }
        resp = create_party_bank_if_missing(base_url, party_id, token, payload["accountNo"], payload, company_id=company_id)
        party_bank_results[f"broker-{code}"] = resp["id"]
    results["party_bank_accounts"] = party_bank_results

    print(json.dumps(results, indent=2))


if __name__ == "__main__":
    try:
        main()
    except HTTPError as exc:
        body = exc.read().decode("utf-8") if exc.fp else ""
        print(f"HTTP {exc.code} {exc.reason}: {body}", file=sys.stderr)
        sys.exit(1)
    except URLError as exc:
        print(f"Request failed: {exc}", file=sys.stderr)
        sys.exit(1)
