#include <fstream>
#include <iostream>
#include <stack>
#include <cmath>
using namespace std;

//Constants
const int ROWS = 2000;//2000
const int COLS = 3000;//3000
const int DIST = 150;//150
const int SIZE = 40000;//40000
//Variables
int map[ROWS][COLS];
int input[ROWS][COLS]; //Make negative if visited
int location[DIST+1][2];
int counted[DIST+1]; //One indexed
double centers[DIST+1][2];
long long votes[DIST+1];
int won = 0;
double loc = 0;

int main(){
	//Start file input and format checking
	ifstream inFile;
	inFile.open("input.txt"); //Either have a batch file rename the file to input.txt or pass in the name as an argument. Renaming seems safer
	if (!inFile.is_open()){
		cout << "Error: Failed to open file." << endl;
		return 1;
	}
	int temp;
	for(int r = 0; r < ROWS; r++){
		for(int c = 0; c < COLS; c++){
			try{
				inFile >> temp;
				if (temp > 0 && temp <= DIST){
					if(counted[temp]==0){
						counted[temp] = 1;
						location[temp][0] = r;
						location[temp][1] = c;
					}
					centers[temp][0]+=r;
					centers[temp][1]+=c;
					input[r][c] = temp;
				} else {
					cout << "Invalid district number" << endl;
					return 1;
				}
			} catch (int e) {
				cout << "Input format error." << endl;
				return 1;
			}
		}
	}
	inFile.close();
	//Load map if no simple errors
	ifstream mapFile;
	mapFile.open("map.txt");
	if (!mapFile.is_open()){
		cout << "Error: Failed to open map file. This is our fault." << endl;
		return 1;
	}
	for(int r = 0; r < ROWS; r++){
		for(int c = 0; c < COLS; c++){
			try{
				mapFile >> map[r][c];
			} catch (int e) {
				cout << "Map file format error. This is our fault" << endl;
				return 1;
			}
		}
	}
	mapFile.close();
	//Evaluate Winners of Districts
	stack<pair<int,int> > dfs;
	for(int i = 1; i < DIST+1; i++){
		centers[i][0]/=SIZE;
		centers[i][1]/=SIZE;
		dfs.push({location[i][0],location[i][1]});
		input[location[i][0]][location[i][1]]*=-1;
		counted[i] = 0;
		while(dfs.size()>0){
			int r = dfs.top().first;
			int c = dfs.top().second;
			dfs.pop();
			counted[i]++;
			votes[i]+=map[r][c];
			loc+=pow(r-centers[i][0],2)+pow(c-centers[i][1],2);
			if(r+1 < ROWS && input[r+1][c] == i){
				input[r+1][c]*=-1;
				dfs.push({r+1,c});
			}
			if(r-1 >= 0 && input[r-1][c] == i){
				input[r-1][c]*=-1;
				dfs.push({r-1,c});
			}
			if(c+1 < COLS && input[r][c+1] == i){
				input[r][c+1]*=-1;
				dfs.push({r,c+1});
			}
			if(c-1 >= 0 && input[r][c-1] == i){
				input[r][c-1]*=-1;
				dfs.push({r,c-1});
			}
		}
		if (counted[i] != SIZE){
			cout << "Incorrect districting (Non contiguous, not enough of one district, etc...): " << endl;
			return 1;
		}
	}
	//calculate votes won
	for(int i = 1; i < DIST+1; i++)
		if (votes[i] > 0) won++;
	//TODO
	cout << won << " " << loc << endl;
	return 0;
}