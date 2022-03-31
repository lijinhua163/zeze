local old_version = "0.9.21-SNAPSHOT"
local new_version = "0.9.22-SNAPSHOT"

local files = {
	{ 1, "build.gradle" },
--	{ 1, "pom.xml" },
	{ 1, "ZezeJava/pom.xml" },
	{ 2, "ZezeJavaTest/pom.xml" },
	{ 2, "ZezexJava/linkd/pom.xml" },
	{ 2, "ZezexJava/server/pom.xml" },
	{ 3, "TestRaft/raft.bat" },
	{ 7, "TestRaft/raft.more.bat" },
	{ 5, "TestRaft/raft.5x6node.bat" },
	{ 2, "TestRaft/GlobalRaft/service&global_raft3.bat" },
	{ 2, "TestRaft/Simulate/simulate.bat" },
}

local old_version_pat = old_version:gsub("%.", "%%."):gsub("%-", "%%-")

local function find_count(str)
	local i, j, n = 0, 0, 0
	while true do
		i, j = str:find(old_version_pat, j + 1)
		if not i then return n end
		n = n + 1
	end
end

local function check_version(filename, change_count)
	local f = io.open(filename, "rb")
	if not f then error("file not found: " .. filename) end
	local s = f:read "*a"
	f:close()

	local count = find_count(s)
	if count ~= change_count then
		error(filename .. ": need " .. change_count .. " '" .. old_version .. "', but found " .. count)
	end
end

local function change_version(filename)
	local f = io.open(filename, "rb")
	local s = f:read "*a"
	f:close()

	io.write("modify " .. filename .. " ... ")
	local t = s:gsub(old_version_pat, new_version)
	if t ~= s then
		local f = io.open(filename, "wb")
		f:write(t)
		f:close()
	end
	print "OK"
end

for _, f in ipairs(files) do
	check_version(f[2], f[1])
end

print "--- CHECK DONE ---"

for _, f in ipairs(files) do
	change_version(f[2])
end

print "=== CHANGE DONE ==="