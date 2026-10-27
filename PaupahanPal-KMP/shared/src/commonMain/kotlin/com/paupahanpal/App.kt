package com.paupahanpal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF172554)
private val Blue = Color(0xFF3157D5)
private val Pale = Color(0xFFF4F6FC)
private val Green = Color(0xFF16794B)
private val Orange = Color(0xFFB45309)
private val Red = Color(0xFFB42318)

private enum class Screen(val label: String) {
    HOME("Home"), ROOMS("Rooms"), RENT("Rent"), REPAIRS("Repairs"), TENANTS("Tenants")
}
private data class Room(val number: String, val tenant: String, val rent: Double, val paid: Double, val state: String) {
    val balance get() = (rent - paid).coerceAtLeast(0.0)
}
private data class Repair(val room: String, val issue: String, val priority: String, var status: String)
private data class TenantEvent(val tenant: String, val category: String, val note: String, val date: String, val followUp: String)

@Composable
fun App() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var selectedTenant by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    var roomFilter by remember { mutableStateOf("All") }
    var showPayment by remember { mutableStateOf(false) }
    var showRepair by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }
    var showFollowUp by remember { mutableStateOf(false) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("GCash") }
    var paymentRoom by remember { mutableStateOf("101") }
    var repairRoom by remember { mutableStateOf("101") }
    var repairTitle by remember { mutableStateOf("") }
    var repairPriority by remember { mutableStateOf("Medium") }
    var noteTenant by remember { mutableStateOf("Maria Santos") }
    var noteText by remember { mutableStateOf("") }
    var noteCategory by remember { mutableStateOf("Conversation") }
    var followUpText by remember { mutableStateOf("") }
    var followUpDue by remember { mutableStateOf("") }
    var toast by remember { mutableStateOf("") }

    val rooms = remember {
        mutableStateListOf(
            Room("101", "Maria Santos", 5500.0, 3000.0, "Occupied"),
            Room("102", "Joshua Reyes", 5200.0, 5200.0, "Occupied"),
            Room("103", "—", 5000.0, 0.0, "Vacant"),
            Room("201", "Angela Cruz", 6000.0, 0.0, "Occupied"),
            Room("202", "Noel Garcia", 5800.0, 2000.0, "Occupied"),
            Room("203", "—", 5800.0, 0.0, "Maintenance")
        )
    }
    val repairs = remember {
        mutableStateListOf(
            Repair("203", "Leaking bathroom faucet", "High", "Open"),
            Repair("101", "Ceiling stain inspection", "Medium", "In progress"),
            Repair("202", "Replace door lock", "Low", "Waiting")
        )
    }
    val events = remember {
        mutableStateListOf(
            TenantEvent("Maria Santos", "Payment promise", "Will pay the remaining balance on Friday.", "Oct 20, 2026", "Oct 23, 2026"),
            TenantEvent("Joshua Reyes", "Payment", "October rent fully paid through GCash.", "Oct 5, 2026", ""),
            TenantEvent("Angela Cruz", "Conversation", "Asked about the move-in inspection.", "Oct 3, 2026", "")
        )
    }
    val outstanding = rooms.sumOf { it.balance }
    val collections = rooms.sumOf { it.paid }
    val openRepairs = repairs.count { it.status != "Resolved" }
    val overdueFollowups = events.count { it.followUp.isNotBlank() && it.followUp < "Oct 21, 2026" }

    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, background = Pale, surface = Color.White)) {
        Scaffold(
            containerColor = Pale,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    Screen.values().forEach { item ->
                        NavigationBarItem(
                            selected = screen == item,
                            onClick = { screen = item; selectedRoom = null; selectedTenant = null },
                            icon = { Text(when(item) { Screen.HOME -> "⌂"; Screen.ROOMS -> "▦"; Screen.RENT -> "₱"; Screen.REPAIRS -> "🔧"; Screen.TENANTS -> "♙" }, fontSize = 18.sp) },
                            label = { Text(item.label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("PaupahanPal", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Landlord companion  •  October 2026", color = Color.Gray, fontSize = 12.sp)
                    }
                    Surface(color = Blue.copy(alpha = .1f), shape = RoundedCornerShape(12.dp)) {
                        Text("My property ▾", Modifier.padding(10.dp), color = Blue, fontSize = 12.sp)
                    }
                }
                if (selectedRoom != null) {
                    RoomDetail(selectedRoom!!, onBack = { selectedRoom = null }, onPayment = { paymentRoom = selectedRoom!!.number; showPayment = true })
                } else if (selectedTenant != null) {
                    TenantDetail(selectedTenant!!, events.filter { it.tenant == selectedTenant }, rooms.firstOrNull { it.tenant == selectedTenant }, onBack = { selectedTenant = null }, onNote = { noteTenant = selectedTenant!!; showNote = true })
                } else {
                    when (screen) {
                        Screen.HOME -> HomeScreen(outstanding, collections, openRepairs, overdueFollowups, rooms, repairs, events,
                            onRoom = { selectedRoom = it }, onRepair = { screen = Screen.REPAIRS },
                            onPayment = { showPayment = true }, onAddRepair = { showRepair = true },
                            onNote = { showNote = true }, onFollowUp = { showFollowUp = true })
                        Screen.ROOMS -> RoomsScreen(rooms, search, { search = it }, roomFilter, { roomFilter = it }, { selectedRoom = it })
                        Screen.RENT -> RentScreen(rooms, onRoom = { selectedRoom = it }, onPayment = { showPayment = true })
                        Screen.REPAIRS -> RepairsScreen(repairs, onAdd = { showRepair = true }, onUpdate = { repair ->
                            val index = repairs.indexOf(repair)
                            if (index >= 0) repairs[index] = repair.copy(status = when(repair.status) { "Open" -> "In progress"; "In progress" -> "Waiting"; "Waiting" -> "Resolved"; else -> "Open" })
                        })
                        Screen.TENANTS -> TenantsScreen(rooms, events, onTenant = { selectedTenant = it }, onNote = { noteTenant = it; showNote = true })
                    }
                }
                if (toast.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(toast, color = Green, fontSize = 12.sp)
                }
            }
        }

        if (showPayment) {
            EntryDialog("Record payment", onDismiss = { showPayment = false }, onSave = {
                val amount = paymentAmount.toDoubleOrNull()
                val idx = rooms.indexOfFirst { it.number == paymentRoom }
                if (amount != null && amount > 0 && idx >= 0) {
                    val r = rooms[idx]
                    rooms[idx] = r.copy(paid = (r.paid + amount).coerceAtMost(r.rent))
                    toast = "Payment of ₱${money(amount)} recorded via $paymentMethod."
                    paymentAmount = ""
                    showPayment = false
                } else toast = "Enter a valid positive payment amount."
            }) {
                Field("Room number", paymentRoom, { paymentRoom = it })
                Field("Amount (₱)", paymentAmount, { paymentAmount = it })
                Picker("Method", paymentMethod, listOf("Cash", "GCash", "Bank transfer", "Other")) { paymentMethod = it }
                Text("Demo note: payments are stored in memory and reset when the app restarts.", fontSize = 11.sp, color = Color.Gray)
            }
        }
        if (showRepair) {
            EntryDialog("Log repair", onDismiss = { showRepair = false }, onSave = {
                if (repairTitle.isNotBlank()) {
                    repairs.add(0, Repair(repairRoom, repairTitle, repairPriority, "Open"))
                    toast = "Repair logged for room $repairRoom."
                    repairTitle = ""; showRepair = false
                } else toast = "Please enter a repair description."
            }) {
                Field("Room number", repairRoom, { repairRoom = it })
                Field("Issue / description", repairTitle, { repairTitle = it })
                Picker("Priority", repairPriority, listOf("Low", "Medium", "High")) { repairPriority = it }
            }
        }
        if (showNote) {
            EntryDialog("Add tenant note", onDismiss = { showNote = false }, onSave = {
                if (noteText.isNotBlank()) {
                    events.add(0, TenantEvent(noteTenant, noteCategory, noteText, "Oct 21, 2026", ""))
                    toast = "Tenant note saved. It is not a payment record."
                    noteText = ""; showNote = false
                } else toast = "Please enter a note."
            }) {
                Picker("Tenant", noteTenant, rooms.filter { it.tenant != "—" }.map { it.tenant }) { noteTenant = it }
                Picker("Category", noteCategory, listOf("Conversation", "Payment promise", "Complaint", "Notice", "Repair", "Other")) { noteCategory = it }
                Field("Note", noteText, { noteText = it })
            }
        }
        if (showFollowUp) {
            EntryDialog("Create follow-up", onDismiss = { showFollowUp = false }, onSave = {
                if (followUpText.isNotBlank() && followUpDue.isNotBlank()) {
                    events.add(0, TenantEvent(noteTenant, "Follow-up", followUpText, "Oct 21, 2026", followUpDue))
                    toast = "Follow-up added for $followUpDue."
                    followUpText = ""; followUpDue = ""; showFollowUp = false
                } else toast = "Enter a follow-up and due date."
            }) {
                Picker("Tenant", noteTenant, rooms.filter { it.tenant != "—" }.map { it.tenant }) { noteTenant = it }
                Field("Follow-up action", followUpText, { followUpText = it })
                Field("Due date (e.g. Oct 25, 2026)", followUpDue, { followUpDue = it })
            }
        }
    }
}

@Composable
private fun HomeScreen(
    outstanding: Double, collections: Double, openRepairs: Int, overdue: Int,
    rooms: List<Room>, repairs: List<Repair>, events: List<TenantEvent>,
    onRoom: (Room) -> Unit, onRepair: () -> Unit, onPayment: () -> Unit,
    onAddRepair: () -> Unit, onNote: () -> Unit, onFollowUp: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { Text("Good afternoon 👋", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink) }
        item { Text("Here is what needs your attention today.", color = Color.Gray, fontSize = 13.sp) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Rent outstanding", "₱${money(outstanding)}", "Across all rooms", Red, Modifier.weight(1f))
                    MetricCard("Collections", "₱${money(collections)}", "Recorded this month", Green, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("Open repairs", openRepairs.toString(), "Need attention", Orange, Modifier.weight(1f))
                    MetricCard("Follow-ups", overdue.toString(), "Overdue reminders", Blue, Modifier.weight(1f))
                }
            }
        }
        item { SectionTitle("Quick actions") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("＋ Record payment", onPayment, Modifier.weight(1f))
                    ActionButton("＋ Log repair", onAddRepair, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("＋ Tenant note", onNote, Modifier.weight(1f))
                    ActionButton("＋ Follow-up", onFollowUp, Modifier.weight(1f))
                }
            }
        }
        item { SectionTitle("Needs attention") }
        items(rooms.filter { it.balance > 0 && it.state == "Occupied" }.sortedByDescending { it.balance }.take(3)) { room ->
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth().clickable { onRoom(room) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Room ${room.number} · ${room.tenant}", fontWeight = FontWeight.SemiBold, color = Ink)
                        Text("Outstanding ₱${money(room.balance)}", color = Red, fontSize = 13.sp)
                    }
                    Text("View ›", color = Blue, fontSize = 12.sp)
                }
            }
        }
        items(repairs.filter { it.status != "Resolved" }.take(2)) { repair ->
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth().clickable { onRepair() }) {
                Column(Modifier.padding(14.dp)) {
                    Text("🔧 Room ${repair.room}: ${repair.issue}", fontWeight = FontWeight.SemiBold, color = Ink)
                    Text("${repair.priority} priority · ${repair.status}", color = Orange, fontSize = 12.sp)
                }
            }
        }
        items(events.filter { it.followUp.isNotBlank() }.take(2)) { event ->
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Follow up with ${event.tenant}", fontWeight = FontWeight.SemiBold, color = Ink)
                    Text("${event.note} · Due ${event.followUp}", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RoomsScreen(rooms: List<Room>, search: String, onSearch: (String) -> Unit, filter: String, onFilter: (String) -> Unit, onRoom: (Room) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        SectionTitle("Properties & rooms")
        Field("Search room or tenant", search, onSearch)
        Spacer(Modifier.height(8.dp))
        Picker("Filter", filter, listOf("All", "Occupied", "Vacant", "Maintenance"), onFilter)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
            items(rooms.filter { (filter == "All" || it.state == filter) && (search.isBlank() || it.number.contains(search, true) || it.tenant.contains(search, true)) }) { room ->
                Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { onRoom(room) }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Room ${room.number}", fontWeight = FontWeight.Bold, color = Ink)
                            Text(room.tenant, fontSize = 13.sp, color = Color.Gray)
                            Text("Rent ₱${money(room.rent)} · Balance ₱${money(room.balance)}", fontSize = 12.sp)
                        }
                        StatusPill(if (room.state == "Vacant") "Vacant" else if (room.balance == 0.0) "Paid" else "Unpaid")
                    }
                }
            }
        }
    }
}

@Composable
private fun RentScreen(rooms: List<Room>, onRoom: (Room) -> Unit, onPayment: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { SectionTitle("Rent ledger") }
        item {
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("October 2026 summary", fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    SummaryLine("Monthly charges", "₱${money(rooms.filter { it.state == "Occupied" }.sumOf { it.rent })}")
                    SummaryLine("Payments recorded", "₱${money(rooms.sumOf { it.paid })}")
                    SummaryLine("Outstanding balance", "₱${money(rooms.sumOf { it.balance })}", Red)
                }
            }
        }
        item { Button(onClick = onPayment, modifier = Modifier.fillMaxWidth()) { Text("＋ Record payment") } }
        item { SectionTitle("Tenant ledger") }
        items(rooms.filter { it.state == "Occupied" }) { room ->
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { onRoom(room) }) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${room.tenant} · Room ${room.number}", fontWeight = FontWeight.SemiBold, color = Ink)
                            Text("Due date: October 5, 2026", color = Color.Gray, fontSize = 12.sp)
                        }
                        StatusPill(if (room.balance == 0.0) "Paid" else if (room.paid > 0) "Partial" else "Overdue")
                    }
                    Spacer(Modifier.height(6.dp))
                    SummaryLine("Charge", "₱${money(room.rent)}")
                    SummaryLine("Payments received", "₱${money(room.paid)}", Green)
                    SummaryLine("Remaining balance", "₱${money(room.balance)}", if (room.balance > 0) Red else Green)
                }
            }
        }
    }
}

@Composable
private fun RepairsScreen(repairs: List<Repair>, onAdd: () -> Unit, onUpdate: (Repair) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { SectionTitle("Repairs & maintenance") }
        item { Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("＋ Log a repair") } }
        items(repairs) { repair ->
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Room ${repair.room}", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(repair.issue, color = Ink, fontWeight = FontWeight.SemiBold)
                        }
                        StatusPill(repair.priority)
                    }
                    Text("Status: ${repair.status}", fontSize = 13.sp, color = Color.Gray)
                    OutlinedButton(onClick = { onUpdate(repair) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (repair.status == "Resolved") "Reopen issue" else "Advance status")
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantsScreen(rooms: List<Room>, events: List<TenantEvent>, onTenant: (String) -> Unit, onNote: (String) -> Unit) {
    val tenants = rooms.filter { it.tenant != "—" }.map { it.tenant }.distinct()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { SectionTitle("Tenants & situation log") }
        item { Text("Open a tenant profile to view their balance and chronological notes.", color = Color.Gray, fontSize = 13.sp) }
        items(tenants) { tenant ->
            val room = rooms.first { it.tenant == tenant }
            val count = events.count { it.tenant == tenant }
            Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { onTenant(tenant) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(tenant, fontWeight = FontWeight.Bold, color = Ink)
                        Text("Room ${room.number} · ₱${money(room.balance)} balance", fontSize = 12.sp, color = Color.Gray)
                        Text("$count timeline event(s)", fontSize = 11.sp, color = Blue)
                    }
                    TextButton(onClick = { onNote(tenant) }) { Text("+ Note") }
                }
            }
        }
    }
}

@Composable
private fun RoomDetail(room: Room, onBack: () -> Unit, onPayment: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { TextButton(onClick = onBack) { Text("‹ Back to rooms") } }
        item { SectionTitle("Room ${room.number}") }
        item {
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryLine("Occupancy", room.state)
                    SummaryLine("Current tenant", room.tenant)
                    SummaryLine("Monthly rent", "₱${money(room.rent)}")
                    SummaryLine("Paid so far", "₱${money(room.paid)}", Green)
                    SummaryLine("Outstanding", "₱${money(room.balance)}", Red)
                    Button(onClick = onPayment, modifier = Modifier.fillMaxWidth()) { Text("Record payment for this room") }
                }
            }
        }
        item { Text("Lease, receipts, photos and detailed history can be added when the database layer is connected.", color = Color.Gray, fontSize = 12.sp) }
    }
}

@Composable
private fun TenantDetail(tenant: String, events: List<TenantEvent>, room: Room?, onBack: () -> Unit, onNote: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
        item { TextButton(onClick = onBack) { Text("‹ Back to tenants") } }
        item { SectionTitle(tenant) }
        item {
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SummaryLine("Room", room?.number ?: "Not assigned")
                    SummaryLine("Monthly rent", "₱${money(room?.rent ?: 0.0)}")
                    SummaryLine("Current balance", "₱${money(room?.balance ?: 0.0)}", Red)
                    Button(onClick = onNote, modifier = Modifier.fillMaxWidth()) { Text("＋ Add timeline note") }
                }
            }
        }
        item { SectionTitle("Situation timeline") }
        if (events.isEmpty()) item { Text("No events recorded yet.", color = Color.Gray) }
        items(events) { event ->
            Surface(color = Color.White, shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(event.category, color = Blue, fontWeight = FontWeight.Bold)
                    Text(event.note, color = Ink)
                    Text(event.date, color = Color.Gray, fontSize = 11.sp)
                    if (event.followUp.isNotBlank()) Text("Follow-up: ${event.followUp}", color = Orange, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Color.Gray, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.Gray, fontSize = 10.sp)
        }
    }
}
@Composable private fun SectionTitle(text: String) {
    Text(text, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
}
@Composable private fun ActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 8.dp)) {
        Text(text, fontSize = 11.sp)
    }
}
@Composable private fun StatusPill(text: String) {
    val color = when (text) { "Paid", "Resolved" -> Green; "Vacant", "Low" -> Blue; "High", "Overdue", "Unpaid" -> Red; else -> Orange }
    Surface(color = color.copy(alpha = .1f), shape = RoundedCornerShape(50)) {
        Text(text, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
@Composable private fun SummaryLine(label: String, value: String, valueColor: Color = Ink) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
@Composable private fun Field(label: String, value: String, onValue: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Surface(color = Color.White, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
            BasicTextField(value, onValue, Modifier.fillMaxWidth().padding(12.dp), singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp), decorationBox = { inner ->
                if (value.isEmpty()) Text("Enter $label", color = Color.LightGray, fontSize = 14.sp)
                inner()
            })
        }
    }
}
@Composable private fun Picker(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selected, modifier = Modifier.weight(1f))
                Text("▾")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
                }
            }
        }
    }
}
@Composable
private fun EntryDialog(title: String, onDismiss: () -> Unit, onSave: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp), content = content) },
        confirmButton = { Button(onClick = onSave) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
private fun money(amount: Double): String {
    val rounded = amount.toLong()
    return rounded.toString().reversed().chunked(3).joinToString(",").reversed()
}
