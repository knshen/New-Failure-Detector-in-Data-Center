#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <cstdlib>
#include <boost/algorithm/string.hpp>
#include <utility>
#include <set>

#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"
#include "ns3/global-route-manager.h"
//#include "ns3/mobility-module.h"
#include "ns3/netanim-module.h"
#include "ns3/assert.h"
#include "ns3/ipv4-global-routing-helper.h"

using namespace std;
using namespace ns3;
using namespace boost;

vector<vector<bool> > readNxNMatrix (std::string adj_mat_file_name);
void saveIP(vector<Ipv4Address> ips);
vector < vector<int> > readRuleFile(string fileName);
vector < pair<int, double> > readServerCrashFile(string fileName);

NS_LOG_COMPONENT_DEFINE ("PingGenericTopologyCreation");

int main (int argc, char *argv[])
{
	// global configure
	string server_crash_file_name = "/home/knshen/server-crash-10min.txt";
	string fileName = "/home/knshen/topo.txt";
	string rulePath = "/home/knshen/rule.txt";
	uint32_t linkCount = 0;

	Config::SetDefault("ns3::Ipv4GlobalRouting::RespondToInterfaceEvents", BooleanValue(true));
	Config::SetDefault("ns3::Ipv4GlobalRouting::RandomEcmpRouting", BooleanValue(true));

	// read topology file
	vector< vector<bool> > matrix = readNxNMatrix (fileName);
	int num_nodes = matrix.size();

	// create nodes
	NodeContainer nodes;   // Declare nodes objects
	nodes.Create (num_nodes);

	// configure p2p link attributes
	PointToPointHelper p2p;
	p2p.SetDeviceAttribute ("DataRate", StringValue ("100Mbps"));
	p2p.SetChannelAttribute ("Delay", StringValue ("100ms"));

	// install internet stack
	InternetStackHelper internet;
	internet.Install (NodeContainer::GetGlobal ());

	// assign IP address
	Ipv4AddressHelper ipv4_n;
	ipv4_n.SetBase ("10.0.0.0", "255.255.255.252");
	vector<Ipv4Address> ips(num_nodes);

	// create links
	for (size_t i = 0; i < matrix.size (); i++)
	{
		for (size_t j = 0; j < matrix[i].size (); j++)
		{
			if (matrix[i][j] == 1)
			{
				NodeContainer n_links = NodeContainer (nodes.Get (i), nodes.Get (j));
				NetDeviceContainer n_devs = p2p.Install (n_links);
				Ipv4InterfaceContainer ic = ipv4_n.Assign (n_devs);
				ips[i] = ic.GetAddress(0);
				ips[j] = ic.GetAddress(1);
				/*
				if(i < 12)
					cout<<i<<": "<<ips[i]<<endl;
				if(j < 12)
					cout<<j<<": "<<ips[j]<<endl;
				*/
				ipv4_n.NewNetwork ();
				linkCount++;
			}

		}
	}


	Ipv4GlobalRoutingHelper::PopulateRoutingTables ();

	/*
	V4PingHelper ping = V4PingHelper (ips[12]);
	ping.SetAttribute ("Interval", TimeValue (Seconds (2))); 
	NodeContainer pingers;
	for(int i=0; i<num_nodes; i++) 
	{
		if(i == 12)
			continue;
		pingers.Add(nodes.Get(i));
	}
	
	ApplicationContainer app = ping.Install(pingers);
	app.Start(Seconds(2));
	app.Stop(Seconds(6));
	*/

	
	vector< vector<ApplicationContainer> > apps;
	for(int i = 0; i < num_nodes; i++)
	{
		vector<ApplicationContainer> ve;
		apps.push_back(ve);
	}

	// create ping apps
	for (int i = 0; i < num_nodes; i++)
	{
		for (int j = 0; j < num_nodes; j++)
		{
			// j -----ping-----> i
			V4PingHelper ping = V4PingHelper (ips[i]);
			ping.SetAttribute ("Interval", TimeValue (Seconds (2))); 
			NodeContainer pingers;
			if (j != i)
				pingers.Add(nodes.Get(j));
			apps[i].push_back(ping.Install(pingers));	
		}
	}

	for (int i = 0; i < num_nodes; i++)
	{
		for(int j = 0; j < num_nodes; j++)
		{
			apps[i][j].Start (Seconds (2.0 + i * 0.015));
			apps[i][j].Stop(Seconds (20.0 + i * 0.015));
		}
		
	}
	

	/*
	// define server crash events
	// #1 server crash
	vector < pair<int, double> > server_crash = readServerCrashFile(server_crash_file_name);
	set<int> ids;
	for (uint32_t i = 0; i < server_crash.size(); i++)
		ids.insert(server_crash[i].first);

	for (uint32_t i = 0; i < apps.size(); i++)
	{
		set<int>::iterator it;
		it = ids.find(i + 12);
		if (it != ids.end())
		{
			// crash !
			// find the exact crash time
			double crash_time = 0;
			for(uint32_t k=0; k<server_crash.size(); k++)
			{
				if(server_crash[k].first == i + 12.0)
					crash_time = server_crash[k].second;
			}
			//cout<< "crash time: "<<crash_time<<endl;
			for (uint32_t j = 0; j < apps[i].size(); j++)
				apps[i][j].Stop(Seconds(crash_time));
		}
		else
		{
			// no crash!
			for (uint32_t j = 0; j < apps[i].size(); j++)
				apps[i][j].Stop(Seconds(600.0));
		}

	}
	*/

	
	// #2  link crash
	// link1: 0 -- 2
	// link2: 3 -- 7
	Ptr<Ipv4> link1 = nodes.Get(0)->GetObject<Ipv4>();
	Ptr<Ipv4> link2 = nodes.Get(3)->GetObject<Ipv4>();
	uint32_t index1 = 1;
	uint32_t index2 = 4;
	double time1 = 2;
	double time2 = 2;
	Simulator::Schedule(Seconds(time1), &Ipv4::SetDown, link1, index1);
	Simulator::Schedule(Seconds(time2), &Ipv4::SetDown, link2, index2);
	

	/*
	// #3 ToR Switch crash
	int crash_tor_id = 6;
	Ptr<Ipv4> tor = nodes.Get(crash_tor_id)->GetObject<Ipv4>();
	double time = 30;
	for(uint32_t index=1; index<=22; index++)
		Simulator::Schedule(Seconds(time), &Ipv4::SetDown, tor, index);
	*/


	// dump
	p2p.EnablePcapAll ("ping", false);
	//AsciiTraceHelper ascii;
	//p2p.EnableAsciiAll (ascii.CreateFileStream ("topo.tr"));
	Simulator::Run ();
	Simulator::Destroy ();

	return 0;
}

vector < pair<int, double> > readServerCrashFile(string fileName)
{
	vector < pair<int, double> > res;
	ifstream server_crash_file;
	server_crash_file.open(fileName.c_str(), ios::in);

	while (!server_crash_file.eof())
	{
		string line;
		getline(server_crash_file, line);
		if (line == "")
			break;
		vector<string> tokens;
		boost::split(tokens, line, boost::is_any_of(" "));
		int id = atoi(tokens[0].c_str());
		double time = atof(tokens[1].c_str());
		pair<int, double> p(id, time);
		res.push_back(p);
	}

	server_crash_file.close();
	return res;
}

void saveIP(vector<Ipv4Address> ips)
{
	for (uint32_t i = 0; i < ips.size(); i++)
	{
		cout << (12 + i) << ": " << ips[i] << endl;
	}
}

vector < vector<int> > readRuleFile(string fileName)
{
	vector < vector<int> > res;
	ifstream rule_file;
	rule_file.open (fileName.c_str (), ios::in);

	while (!rule_file.eof())
	{
		vector<int> tmp;
		string line;
		getline(rule_file, line);
		vector<string> tokens;
		vector<string> masters;
		if (line == "")
			break;
		boost::split(tokens, line, boost::is_any_of(":"));
		boost::split(masters, tokens[1], boost::is_any_of(" "));
		//cout << tokens[1] << endl;
		for (uint32_t i = 0; i < masters.size(); i++)
			tmp.push_back(atoi(masters[i].c_str()));

		res.push_back(tmp);
	}

	rule_file.close ();
	return res;
}

vector< vector<bool> > readNxNMatrix (std::string adj_mat_file_name)
{
	ifstream adj_mat_file;
	adj_mat_file.open (adj_mat_file_name.c_str (), ios::in);
	if (adj_mat_file.fail ())
	{
		NS_FATAL_ERROR ("File " << adj_mat_file_name.c_str () << " not found");
	}
	vector<vector<bool> > array;
	int i = 0;
	int n_nodes = 0;

	while (!adj_mat_file.eof ())
	{
		string line;
		getline (adj_mat_file, line);
		if (line == "")
		{
			NS_LOG_WARN ("WARNING: Ignoring blank row in the array: " << i);
			break;
		}

		istringstream iss (line);
		bool element;
		vector<bool> row;
		int j = 0;

		while (iss >> element)
		{
			row.push_back (element);
			j++;
		}

		if (i == 0)
		{
			n_nodes = j;
		}

		if (j != n_nodes )
		{
			NS_LOG_ERROR ("ERROR: Number of elements in line " << i << ": " << j << " not equal to number of elements in line 0: " << n_nodes);
			NS_FATAL_ERROR ("ERROR: The number of rows is not equal to the number of columns! in the adjacency matrix");
		}
		else
		{
			array.push_back (row);
		}
		i++;
	}

	if (i != n_nodes)
	{
		NS_LOG_ERROR ("There are " << i << " rows and " << n_nodes << " columns.");
		NS_FATAL_ERROR ("ERROR: The number of rows is not equal to the number of columns! in the adjacency matrix");
	}

	adj_mat_file.close ();
	return array;
}
