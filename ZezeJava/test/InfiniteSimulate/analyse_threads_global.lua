-- ��Ҫ��ǰ�������� simulate.bat ����, ��������ϴη���ʱ�����ջ��Ϣ(stack_global.log)

print "============================================================ BEGIN"
local f = io.popen("jps", "rb")
local s = f:read "*a"
f:close()

local pid_global = s:match "(%d+)%s+GlobalCacheManager[Async]*Server[\r\n]"
-- local pid_glo = s:match "(%d+)%s+GlobalCacheManager[Async]*Server[\r\n]"
if pid_global then
	f = io.popen("jstack -l -e " .. pid_global, "rb")
	s = f:read "*a"
	f:close()

	f = io.open("stack_global.log", "wb")
	f:write(s)
	f:close()
else
	print "û���ҵ� GlobalCacheManager(Async)Server ����"
	f = io.open("stack_global.log", "rb")
	if not f then return end
	print "�� stack_global.log ��ȡ�߳�ջ��Ϣ"
	s = f:read "*a"
	f:close()
	print "------------------------------------------------------------"
end
f = nil

local threads = {} -- threadName, lines, text, wait, owns
local threadName
for line in s:gmatch "[^\r\n]+" do
	line = line:gsub("%s+$", "")
	if line == "" then
		if threadName then
			threads[threadName].text = table.concat(threads[threadName].lines, "\n")
			threadName = nil
		end
	else
		local first = line:sub(1, 1)
		if first == " " or first == "\t" then
			if threadName then
				threads[threadName].lines[#threads[threadName].lines + 1] = line
				local wt = line:match "%- parking to wait for  <(.-)>"
				if wt then threads[threadName].wait = wt end
				local ow = line:match "%- <(.-)>"
				if ow then threads[threadName].owns[ow] = true end
			end
		elseif first == "\"" then
			if threadName then
				threads[threadName].text = table.concat(threads[threadName].lines, "\n")
			end
			threadName = line:match "\"(.-)\""
			threads[threadName] = { threadName = threadName, lines = { line }, owns={} }
		end
	end
end
if threadName then
	threads[threadName].text = table.concat(threads[threadName].lines, "\n")
end

local function findDeadLock(lock, firstThreadName)
	for threadName, thread in pairs(threads) do
		if thread.owns[lock] then
			if threadName == firstThreadName then
				print "��������!!!"
				print("    at " .. threadName)
				return true
			end
			if thread.wait then
				local r = findDeadLock(thread.wait, firstThreadName)
				if r then
					print("    at " .. threadName)
					return r
				end
			end
			return false
		end
	end
end
local foundDeadLock
for threadName, thread in pairs(threads) do
	if thread.wait then
		if findDeadLock(thread.wait, threadName) then
			print("    at " .. threadName)
			foundDeadLock = true
		end
	end
end
if not foundDeadLock then
	print "û�з�������"
end

local knowns = {
	{ "��Load�ȴ�Record1��",                           ".EnterFairLock(", ".Load(" },
	{ "��Load�ȴ�Acquire(Share)�ظ�",                  ".Acquire(", ".Load(" },
	{ "��_lock_and_check_�ȴ�Lockeyд��",              "Lockey.EnterWriteLock(", "._lock_and_check_(" },
	{ "��_check_�ȴ�Record1��",                        ".EnterFairLock(", "._check_(" },
	{ "��_check_�ȴ�Acquire(Modify)�ظ�",              ".Acquire(", "._check_(" },
	{ "��ReduceInvalid�ȴ�Lockeyд��",                 ".EnterWriteLock(", ".ReduceInvalid(" },
	{ "��ReduceInvalid�ȴ�Record1��",                  ".EnterFairLock(", ".ReduceInvalid(" },
	{ "��ReduceInvalidAllLocalOnly�ȴ�Lockeyд��",     ".EnterWriteLock(", ".ReduceInvalidAllLocalOnly(" },
	{ "��TableCache.CleanNow�ȴ�Acquire(Invalid)�ظ�", ".Acquire(", ".CleanNow(" },
	{ "��TableCache.CleanNow��ȴ��´�ѭ��",           ".CleanNow(TableCache.java:166)" },
	{ "��__TryWaitFlushWhenReduce��ȴ�sleep",         ".__TryWaitFlushWhenReduce(Application.java:341)" },
	{ "��Checkpoint�̵߳ȴ���ʱ��",                    ".Object.wait(", ".Checkpoint.Run(" },
	{ "��Selector�̵߳ȴ�NIO�¼�",                     ".Selector.run(Selector.java:67)" },
	{ "AchillesHeelDaemon�߳�",                        "(AchillesHeelDaemon.java:146)" },
	{ "���õȴ�(���߳�)",                              ".Object.wait(", ".main(GlobalCacheManagerServer.java:" },
}

local needKnowns = {
	"at Zeze.",
	"at Infinite.",
}

local counts = {}
for _, thread in pairs(threads) do
	local isKnown
	for i, known in ipairs(knowns) do
		local found = true
		for j = 2, 999 do
			if not known[j] then break end
			if not thread.text:find(known[j], 1, true) then
				found = false
				break
			end
		end
		if found then
			counts[i] = (counts[i] or 0) + 1
			isKnown = true
			break
		end
	end
	if not isKnown then
		local found
		for _, needKnown in ipairs(needKnowns) do
			if thread.text:find(needKnown, 1, true) then
				found = true
				break
			end
		end
		if found then
			counts[0] = (counts[0] or 0) + 1
			if not f then f = io.open("stack_global_others.log", "wb") end
			f:write(thread.text)
			f:write "\n\n"
		else
			counts[-1] = (counts[-1] or 0) + 1
		end
	end
end
if f then f:close() end
for i, known in ipairs(knowns) do
	if counts[i] then
		print(string.format("%4d ���߳� %s", counts[i], known[1]))
	end
end
print "------------------------------------------------------------"
if counts[0] then
	print(string.format("%4d ���߳� δ֪ (ͨ��stack_global_others.log�����߳���Ϣ)", counts[0]))
end
if counts[-1] then
	print(string.format("%4d ���߳� ��Zeze��Infinite����,���̳߳��еĿ����߳�", counts[-1]))
end
print "============================================================ END"
